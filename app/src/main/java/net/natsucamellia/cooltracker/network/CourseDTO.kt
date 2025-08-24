package net.natsucamellia.cooltracker.network

import com.google.gson.annotations.SerializedName

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/courses">Course API Documentation</a>
 */
data class CourseDTO(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("is_public")
    val isPublic: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("course_code")
    val courseCode: String,
)