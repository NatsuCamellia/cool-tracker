package net.natsucamellia.cooltracker.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CoolApiService {
    /**
     * Get the the profile of the corresponding user of [cookies].
     * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/users#method.profile.settings">User API Documentation</a>,
     * note that "self" refers to the current user id.
     */
    @GET("users/self/profile")
    suspend fun getCurrentUserProfile(
        @Header("Cookie") cookies: String
    ): Response<ProfileDTO>

    /**
     * Get up to 100, which is maximum and should be enough, active courses of the corresponding user of [cookies].
     * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/courses#method.courses.user_index">Course API Documentation</a>
     */
    @GET("courses?enrollment_state=active&per_page=100")
    suspend fun getActiveCourses(
        @Header("Cookie") cookies: String
    ): Response<List<CourseDTO>>

    /**
     * Get up to 100, which is maximum and should be enough, assignments of the course with [courseId].
     * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/assignments#method.assignments_api.index">Assignment API Documentation</a>
     */
    @GET("courses/{courseId}/assignments?per_page=100&include[]=submission")
    suspend fun getCourseAssignments(
        @Header("Cookie") cookies: String,
        @Path("courseId") courseId: Int
    ): Response<List<AssignmentDTO>>
}