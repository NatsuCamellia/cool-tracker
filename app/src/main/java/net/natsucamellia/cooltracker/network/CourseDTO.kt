package net.natsucamellia.cooltracker.network

import com.google.gson.annotations.SerializedName

data class CourseDTO(
    val id: Int,
    val name: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("course_code")
    val courseCode: String,
)