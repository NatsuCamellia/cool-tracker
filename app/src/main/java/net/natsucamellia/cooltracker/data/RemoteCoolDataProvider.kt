package net.natsucamellia.cooltracker.data

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile
import net.natsucamellia.cooltracker.network.CoolApiService
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface RemoteCoolDataProvider {
    suspend fun getUserProfile(cookies: String): Profile?
    suspend fun getActiveCourses(cookies: String): List<Course>?
}

class RemoteCoolDataProviderImpl(
    private val coolApiService: CoolApiService
) : RemoteCoolDataProvider {

    override suspend fun getUserProfile(cookies: String): Profile? {
        val response = coolApiService.getCurrentUserProfile(cookies)
        if (!response.isSuccessful) {
            Log.d("NetworkCoolRepository", "getRemoteProfile: $response")
            Log.e("NetworkCoolRepository", "getRemoteProfile: ${response.errorBody()}")
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
        )
    }

    override suspend fun getActiveCourses(cookies: String): List<Course>? {
        val response = coolApiService.getActiveCourses(cookies)
        if (!response.isSuccessful) {
            // The request failed
            Log.d("NetworkCoolRepository", "getRemoteProfile: $response")
            Log.e("NetworkCoolRepository", "getRemoteProfile: ${response.errorBody()}")
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
                    val assignments = getRemoteCourseAssignments(it.id, cookies)
                    return@async if (assignments == null) {
                        null
                    } else {
                        Course(
                            id = it.id,
                            name = it.name,
                            isPublic = it.isPublic,
                            courseCode = it.courseCode,
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
    @OptIn(ExperimentalTime::class)
    private suspend fun getRemoteCourseAssignments(
        courseId: Int,
        cookies: String
    ): List<Assignment>? {
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