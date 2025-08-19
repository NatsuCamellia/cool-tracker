package net.natsucamellia.cooltracker.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CoolApiService {
    @GET("users/self/profile")
    suspend fun getCurrentUserProfile(
        @Header("Cookie") cookies: String?
    ): Response<ProfileDTO>

    @GET("courses?enrollment_state=active")
    suspend fun getActiveCourses(
        @Header("Cookie") cookies: String?
    ): Response<List<CourseDTO>>

    @GET("courses/{courseId}/assignments")
    suspend fun getCourseAssignments(
        @Header("Cookie") cookies: String?,
        @Path("courseId") courseId: Int
    ): Response<List<AssignmentDTO>>
}