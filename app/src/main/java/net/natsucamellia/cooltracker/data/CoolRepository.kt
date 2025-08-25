package net.natsucamellia.cooltracker.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.auth.LoginState
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile
import net.natsucamellia.cooltracker.network.CoolApiService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface CoolRepository {
    /**
     * Get the current user's profile information.
     * @return user's profile information, null if failed.
     */
    fun getUserProfile(): Flow<Profile?>

    /**
     * Get the current user's active courses.
     * @return list of active courses, null if failed.
     */
    fun getActiveCourses(): Flow<List<Course>?>
}

class NetworkCoolRepository(
    private val coolApiService: CoolApiService,
    private val authManager: AuthManager
) : CoolRepository {
    private var userSessionCookies: String? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collect {
                userSessionCookies = if (it is LoginState.LoggedIn) {
                    it.cookies
                } else {
                    null
                }
            }
        }
    }

    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserProfile(): Flow<Profile?> = authManager.loginState.flatMapLatest { state ->
        when (state) {
            is LoginState.LoggedIn -> {
                val cookies = state.cookies
                val profile = getRemoteProfile(cookies)
                flowOf(profile)
            }

            else -> {
                flowOf(null)
            }
        }
    }

    /**
     * Get the current user's active courses from NTU COOL API.
     * @return list of active courses flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveCourses(): Flow<List<Course>?> =
        authManager.loginState.flatMapLatest { state ->
            when (state) {
                is LoginState.LoggedIn -> {
                    val cookies = state.cookies
                    val courses = getRemoteActiveCourses(cookies)
                    flowOf(courses)
                }

                else -> {
                    flowOf(null)
                }
            }
        }

    private suspend fun getRemoteProfile(cookies: String): Profile? {
        val response = coolApiService.getCurrentUserProfile(cookies)
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
            // The request failed
            Log.d("NetworkCoolRepository", "getRemoteProfile: $response")
            Log.e("NetworkCoolRepository", "getRemoteProfile: ${response.errorBody()}")
            null
        }
    }

    private suspend fun getRemoteActiveCourses(cookies: String): List<Course>? {
        val response = coolApiService.getActiveCourses(cookies)
        return if (response.isSuccessful) {
            val courseDTOs = response.body()
            if (courseDTOs != null) {
                coroutineScope {
                    // Get assignments for each course, asynchronously, for faster result.
                    // Map the courses to Jobs and wait all the results.
                    courseDTOs.map {
                        async {
                            val assignments = getRemoteCourseAssignments(it.id)
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
                // Empty response body, probably failed
                null
            }
        } else {
            // The request failed
            Log.d("NetworkCoolRepository", "getRemoteActiveCourses: $response")
            Log.e("NetworkCoolRepository", "getRemoteActiveCourses: ${response.errorBody()}")
            null
        }
    }

    /**
     * Get the assignments for the course with [courseId] from NTU COOL API.
     * @return list of assignments, null if failed.
     */
    @OptIn(ExperimentalTime::class)
    private suspend fun getRemoteCourseAssignments(courseId: Int): List<Assignment>? {
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
}