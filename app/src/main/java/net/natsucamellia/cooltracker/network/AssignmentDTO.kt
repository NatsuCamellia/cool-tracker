package net.natsucamellia.cooltracker.network

import com.google.gson.annotations.SerializedName

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/assignments">Assignment API Documentation</a>
 */
data class AssignmentDTO(
    val id: Int,
    @SerializedName("due_at")
    val dueAt: String?,
    @SerializedName("points_possible")
    val pointsPossible: Double,
    @SerializedName("created_at")
    val createdAt: String,
    val name: String,
    val submission: SubmissionDTO,
    @SerializedName("html_url")
    val htmlUrl: String
)

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/submissions">Submission API Documentation</a>
 */
data class SubmissionDTO(
    val id: Int,
    @SerializedName("workflow_state")
    val workflowState: String
)