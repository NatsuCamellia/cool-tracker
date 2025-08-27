package net.natsucamellia.cooltracker.data.remote

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.natsucamellia.cooltracker.data.remote.api.CoolApiService
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.Profile
import kotlin.time.Instant

interface RemoteCoolDataProvider {
    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information, null if failed.
     */
    suspend fun getUserProfile(cookies: String): Profile?

    /**
     * Get the current user's active courses with assignments from NTU COOL API.
     * @return list of active courses with assignments, null if failed.
     */
    suspend fun getActiveCoursesWithAssignments(cookies: String): List<CourseWithAssignments>?
}

class RemoteCoolDataProviderImpl(
    private val coolApiService: CoolApiService
) : RemoteCoolDataProvider {

    override suspend fun getUserProfile(cookies: String): Profile? {
        val response = try {
            coolApiService.getCurrentUserProfile(cookies)
        } catch (e: Exception) {
            Log.e(TAG, "getUserProfile: $e")
            null
        }

        if (response == null) {
            return null
        }

        if (!response.isSuccessful) {
            Log.d(TAG, "getUserProfile: $response")
            Log.e(TAG, "getUserProfile: ${response.errorBody()}")
            return null
        }

        val profileDTO = response.body()
        if (profileDTO == null) {
            return null
        }

        return Profile(
            id = profileDTO.id,
            name = profileDTO.name,
            bio = profileDTO.bio,
            primaryEmail = profileDTO.primaryEmail,
            avatarUrl = profileDTO.avatarUrl
                ?: "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
        )
    }

    override suspend fun getActiveCoursesWithAssignments(cookies: String): List<CourseWithAssignments>? {
        val response = try {
            coolApiService.getActiveCourses(cookies)
        } catch (e: Exception) {
            Log.e(TAG, "getActiveCoursesWithAssignments: $e")
            null
        }

        if (response == null) {
            return null
        }

        if (!response.isSuccessful) {
            // The request failed
            Log.d(TAG, "getActiveCoursesWithAssignments: $response")
            Log.e(TAG, "getActiveCoursesWithAssignments: ${response.errorBody()}")
            return null
        }

        val courseDTOs = response.body()
        if (courseDTOs == null) {
            return null
        }

        return coroutineScope {
            // Get assignments for each course, asynchronously, for faster result.
            // Map the courses to Jobs and wait all the results.
            courseDTOs.map {
                async {
                    val assignments = getCourseAssignments(it.id, cookies)
                    return@async if (assignments == null) {
                        null
                    } else {
                        CourseWithAssignments(
                            course = Course(
                                id = it.id,
                                name = it.name,
                                isPublic = it.isPublic,
                                courseCode = it.courseCode
                            ),
                            assignments = assignments
                        )

                    }
                }
            }.awaitAll()
                // Drop those failed jobs
                .filterNotNull()
        }
    }

    /**
     * Get the assignments for the course with [courseId] from NTU COOL API.
     * @return list of assignments, null if failed.
     */
    private suspend fun getCourseAssignments(
        courseId: Int,
        cookies: String
    ): List<Assignment>? {
        val response = try {
            coolApiService.getCourseAssignments(cookies, courseId)
        } catch (e: Exception) {
            Log.e(TAG, "getCourseAssignments: $e")
            null
        }

        if (response == null) {
            return null
        }

        return if (response.isSuccessful) {
            val assignmentDTOs = response.body()
            if (assignmentDTOs == null) {
                return null
            }

            // Map the DTOs to Assignment objects
            assignmentDTOs.mapNotNull {
                if (it.dueAt == null) {
                    // The due date is unspecified, skip this assignment
                    // TODO: Maybe we need to show these assignments in the future.
                    null
                } else {
                    Assignment(
                        id = it.id,
                        courseId = it.courseId,
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
            Log.e(TAG, "getCourseAssignments: ${response.errorBody()}")
            null
        }
    }

    companion object {
        private const val TAG = "NetworkCoolRepository"
    }
}