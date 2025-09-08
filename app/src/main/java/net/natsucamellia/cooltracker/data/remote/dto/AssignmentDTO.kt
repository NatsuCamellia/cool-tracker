package net.natsucamellia.cooltracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/assignments">Assignment API Documentation</a>
 */
data class AssignmentDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("due_at") val dueAt: String?,
    @SerializedName("lock_at") val lockAt: String?,
    @SerializedName("points_possible") val pointsPossible: Double,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("name") val name: String,
    @SerializedName("course_id") val courseId: Int,
    @SerializedName("submission") val submission: SubmissionDTO,
    @SerializedName("html_url") val htmlUrl: String
)

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/submissions">Submission API Documentation</a>
 */
data class SubmissionDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("workflow_state") val workflowState: String
)