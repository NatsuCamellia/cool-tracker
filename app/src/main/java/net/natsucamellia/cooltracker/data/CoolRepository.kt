package net.natsucamellia.cooltracker.data

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant
import net.natsucamellia.cooltracker.crypto.KeystoreManager
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.network.CoolApiService
import androidx.core.content.edit
import net.natsucamellia.cooltracker.model.Profile

interface CoolRepository {
    fun saveUserSessionCookies(cookies: String?)
    fun clearUserSessionCookies()
    suspend fun loadStoredUserSessionCookies(): Boolean
    suspend fun getUserProfile(): Profile?
    suspend fun getActiveCourses(): List<Course>?
}

class NetworkCoolRepository(
    private val coolApiService: CoolApiService,
    private val sharedPref: SharedPreferences,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) : CoolRepository {
    private var userSessionCookies: String? = null

    override fun saveUserSessionCookies(cookies: String?) {
        userSessionCookies = cookies
        if (cookies != null) {
            val encryptedPair = keystoreManager.encrypt(cookies)
            encryptedPair?.let { (encryptedCookies, iv) ->
                sharedPref.edit {
                    putString(KEY_ENCRYPTED_COOKIES, encryptedCookies)
                    putString(KEY_IV, iv)
                    apply()
                }
                Log.d(TAG, "User session cookies saved successfully.")
            } ?: run {
                Log.e(TAG, "Failed to encrypt cookies.")
            }
        }
    }

    override fun clearUserSessionCookies() {
        userSessionCookies = null
        sharedPref.edit {
            remove(KEY_ENCRYPTED_COOKIES)
            remove(KEY_IV)
            apply()
        }
        Log.d(TAG, "User session cookies cleared successfully.")
    }

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

    override suspend fun getUserProfile(): Profile? {
        val response = coolApiService.getCurrentUserProfile(userSessionCookies)
        return if (response.isSuccessful) {
            val profileDTO = response.body()
            if (profileDTO != null) {
                Profile(
                    id = profileDTO.id,
                    name = profileDTO.name,
                    bio = profileDTO.bio,
                    primaryEmail = profileDTO.primaryEmail,
                    avatarUrl = profileDTO.avatarUrl
                )
            } else {
                null
            }
        } else {
            Log.d("NetworkCoolRepository", "getCurrentUserProfile: $response")
            Log.e("NetworkCoolRepository", "getCurrentUserProfile: ${response.errorBody()}")
            null
        }
    }

    override suspend fun getActiveCourses(): List<Course>? {
        val response = coolApiService.getActiveCourses(userSessionCookies)
        return if (response.isSuccessful) {
            val courseDTOs = response.body() ?: emptyList()
            Log.d("NetworkCoolRepository", "getActiveCourses: $courseDTOs")
            coroutineScope {
                courseDTOs.map {
                    async {
                        val assignments = getCourseAssignments(it.id)
                        if (assignments != null) {
                            Course(
                                id = it.id,
                                name = it.name,
                                courseCode = it.courseCode,
                                assignments = assignments
                            )
                        } else {
                            null
                        }
                    }
                }.awaitAll()
                    .filterNotNull()
            }
        } else {
            Log.d("NetworkCoolRepository", "getActiveCourses: $response")
            Log.e("NetworkCoolRepository", "getActiveCourses: ${response.errorBody()}")
            null
        }
    }

    private suspend fun getCourseAssignments(courseId: Int): List<Assignment>? {
        val response = coolApiService.getCourseAssignments(userSessionCookies, courseId)

        return if (response.isSuccessful) {
            val assignmentDTOs = response.body() ?: emptyList()
            Log.d("NetworkCoolRepository", "getCourseAssignments: $assignmentDTOs")

            assignmentDTOs.mapNotNull {
                if (it.dueAt == null) {
                    null
                } else {
                    Assignment(
                        id = it.id,
                        name = it.name,
                        dueTime = Instant.parse(it.dueAt),
                        pointsPossible = it.pointsPossible,
                        createdTime = Instant.parse(it.createdAt),
                        submissions = listOf()
                    )
                }
            }
        } else {
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