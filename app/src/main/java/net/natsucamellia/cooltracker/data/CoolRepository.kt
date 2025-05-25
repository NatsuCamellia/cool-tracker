package net.natsucamellia.cooltracker.data

import android.util.Log
import kotlinx.datetime.Instant
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.network.CoolApiService

interface CoolRepository {
    fun saveUserSessionCookies(cookies: String?)
    suspend fun getActiveCourses(): List<Course>
}

class NetworkCoolRepository(
    private val coolApiService: CoolApiService
) : CoolRepository {
    private var userSessionCookies: String? = null

    override fun saveUserSessionCookies(cookies: String?) {
        userSessionCookies = cookies
    }

    override suspend fun getActiveCourses(): List<Course> {
        // TODO: return optional result
        val response = coolApiService.getActiveCourses(userSessionCookies)
        return if (response.isSuccessful) {
            val courseDTOs = response.body() ?: emptyList()
            Log.d("NetworkCoolRepository", "getActiveCourses: $courseDTOs")
            courseDTOs.map {
                Course(
                    id = it.id,
                    name = it.name,
                    courseCode = it.courseCode,
                    assignments = getCourseAssignments(it.id)
                )
            }
        } else {
            Log.e("NetworkCoolRepository", "getActiveCourses: ${response.errorBody()}")
            listOf()
        }
    }

    private suspend fun getCourseAssignments(courseId: Int): List<Assignment> {
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
            listOf()
        }
    }

}