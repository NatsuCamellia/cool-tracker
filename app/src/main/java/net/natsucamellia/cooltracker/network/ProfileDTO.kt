package net.natsucamellia.cooltracker.network

import com.google.gson.annotations.SerializedName

data class ProfileDTO(
    val id: Int,
    val name: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("sortable_name")
    val sortableName: String,
    val title: String?,
    val bio: String?,
    @SerializedName("primary_email")
    val primaryEmail: String,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
)