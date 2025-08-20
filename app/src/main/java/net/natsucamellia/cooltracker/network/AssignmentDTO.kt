package net.natsucamellia.cooltracker.network

import com.google.gson.annotations.SerializedName

data class AssignmentDTO(
    val id: Int,
    @SerializedName("due_at")
    val dueAt: String?,
    @SerializedName("points_possible")
    val pointsPossible: Double,
    @SerializedName("created_at")
    val createdAt: String,
    val name: String,
    @SerializedName("has_submitted_submissions")
    val hasSubmittedSubmissions: Boolean
)