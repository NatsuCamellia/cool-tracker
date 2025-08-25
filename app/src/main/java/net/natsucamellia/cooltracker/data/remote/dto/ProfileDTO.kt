package net.natsucamellia.cooltracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * @see <a href="https://developerdocs.instructure.com/services/canvas/resources/users">User API Documentation</a>,
 */
data class ProfileDTO(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("sortable_name")
    val sortableName: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("primary_email")
    val primaryEmail: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
)