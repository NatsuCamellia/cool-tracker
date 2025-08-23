package net.natsucamellia.cooltracker.data

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.natsucamellia.cooltracker.crypto.KeystoreManager
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile
import net.natsucamellia.cooltracker.network.CoolApiService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface CoolRepository {
    /** Save user's session cookies to local storage */
    fun saveUserSessionCookies(cookies: String)

    /** Clear user's session cookies from local storage */
    fun clearUserSessionCookies()

    /**
     * Load user's session cookies to local storage.
     *
     * @return true if cookies are loaded successfully, false otherwise.
     */
    suspend fun loadStoredUserSessionCookies(): Boolean

    /**
     * Get the current user's profile information.
     * @return user's profile information, null if failed.
     */
    suspend fun getUserProfile(): Profile?

    /**
     * Get the current user's active courses.
     * @return list of active courses, null if failed.
     */
    suspend fun getActiveCourses(): List<Course>?
}

class NetworkCoolRepository(
    private val coolApiService: CoolApiService,
    private val sharedPref: SharedPreferences,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) : CoolRepository {
    // TODO: Maybe use a CookieManager to store cookies?
    //  Since CoolRepository interface should not contain any implementation details,
    /** User's session cookies, write-only from outside with [saveUserSessionCookies] and [clearUserSessionCookies] */
    private var userSessionCookies: String? = null

    /**
     * Encrypt with Android Keystore and save user's session cookies to [SharedPreferences].
     */
    override fun saveUserSessionCookies(cookies: String) {
        userSessionCookies = cookies
        val encryptedPair = keystoreManager.encrypt(cookies)
        encryptedPair?.let { (encryptedCookies, iv) ->
            sharedPref.edit {
                putString(KEY_ENCRYPTED_COOKIES, encryptedCookies)
                // Also store the initialization vector (IV) for decryption
                putString(KEY_IV, iv)
                apply()
            }
            Log.d(TAG, "User session cookies saved successfully.")
        } ?: run {
            Log.e(TAG, "Failed to encrypt cookies.")
        }
    }

    /**
     * Clear user's session cookies from [SharedPreferences].
     */
    override fun clearUserSessionCookies() {
        userSessionCookies = null
        sharedPref.edit {
            remove(KEY_ENCRYPTED_COOKIES)
            remove(KEY_IV)
            apply()
        }
        Log.d(TAG, "User session cookies cleared successfully.")
    }

    /**
     * Load user's session cookies from [SharedPreferences].
     */
    override suspend fun loadStoredUserSessionCookies(): Boolean {
        val encryptedData = sharedPref.getString(KEY_ENCRYPTED_COOKIES, null)
        val iv = sharedPref.getString(KEY_IV, null)

        return if (encryptedData != null && iv != null) {
            keystoreManager.decrypt(encryptedData, iv)?.let { cookies ->
                val response = coolApiService.getActiveCourses(cookies)
                userSessionCookies = cookies
                response.isSuccessful
            } ?: false
        } else {
            Log.d(TAG, "No stored user session cookies found.")
            false
        }
    }

    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information, null if failed.
     */
    override suspend fun getUserProfile(): Profile? {
        val cookies = userSessionCookies
        if (cookies == null) {
            return null
        }
        val response = coolApiService.getCurrentUserProfile(cookies)
        return if (response.isSuccessful) {
            val profileDTO = response.body()
            if (profileDTO == null) {
                // Empty response body, probably failed
                return null
            }
            Profile(
                id = profileDTO.id,
                name = profileDTO.name,
                bio = profileDTO.bio,
                primaryEmail = profileDTO.primaryEmail,
                avatarUrl = profileDTO.avatarUrl
            )
        } else {
            // The request failed
            Log.d("NetworkCoolRepository", "getCurrentUserProfile: $response")
            Log.e("NetworkCoolRepository", "getCurrentUserProfile: ${response.errorBody()}")
            null
        }
    }

    /**
     * Get the current user's active courses from NTU COOL API.
     * @return list of active courses, null if failed.
     */
    override suspend fun getActiveCourses(): List<Course>? {
        val cookies = userSessionCookies
        if (cookies == null) {
            return null
        }
        val response = coolApiService.getActiveCourses(cookies)
        return if (response.isSuccessful) {
            val courseDTOs = response.body()
            if (courseDTOs == null) {
                // Empty response body, probably failed
                return null
            }
            coroutineScope {
                // Get assignments for each course, asynchronously, for faster result.
                // Map the courses to Jobs and wait all the results.
                courseDTOs.map {
                    async {
                        val assignments = getCourseAssignments(it.id)
                        if (assignments != null) {
                            Course(
                                id = it.id,
                                name = it.name,
                                isPublic = it.isPublic,
                                courseCode = it.courseCode,
                                assignments = assignments
                            )
                        } else {
                            // Failed to get assignments
                            null
                        }
                    }
                }.awaitAll()
                    // Drop those failed jobs
                    .filterNotNull()
            }
        } else {
            // The request failed
            Log.d("NetworkCoolRepository", "getActiveCourses: $response")
            Log.e("NetworkCoolRepository", "getActiveCourses: ${response.errorBody()}")
            null
        }
    }

    /**
     * Get the assignments for the course with [courseId] from NTU COOL API.
     * @return list of assignments, null if failed.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun getCourseAssignments(courseId: Int): List<Assignment>? {
        val cookies = userSessionCookies
        if (cookies == null) {
            return null
        }
        val response = coolApiService.getCourseAssignments(cookies, courseId)

        return if (response.isSuccessful) {
            val assignmentDTOs = response.body()
            if (assignmentDTOs == null) {
                return null
            }

            // Map the DTOs to Assignment objects
            assignmentDTOs.mapNotNull {
                if (it.dueAt == null) {
                    // The due date is unspecified, skip this assignment
                    // TODO(Maybe we need to show these assignments in the future.)
                    null
                } else {
                    Assignment(
                        id = it.id,
                        name = it.name,
                        dueTime = Instant.parse(it.dueAt),
                        pointsPossible = it.pointsPossible,
                        createdTime = Instant.parse(it.createdAt),
                        submitted = it.submission.workflowState != "unsubmitted",
                        htmlUrl = it.htmlUrl
                    )
                }
            }
        } else {
            // The request failed
            Log.e("NetworkCoolRepository", "getCourseAssignments: ${response.errorBody()}")
            null
        }
    }

    companion object {
        private const val TAG = "NetworkCoolRepository"
        private const val KEY_ENCRYPTED_COOKIES = "encrypted_cool_cookies"
        private const val KEY_IV = "cool_cookies_iv"
    }
}