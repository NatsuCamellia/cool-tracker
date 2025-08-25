package net.natsucamellia.cooltracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @sample [sampleProfile]
 */
@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey
    val id: Int,
    val name: String,
    val bio: String?,
    val primaryEmail: String,
    val avatarUrl: String
)

val sampleProfile: Profile = Profile(
    id = 1,
    name = "Sample User",
    bio = "Hello!",
    primaryEmail = "sample_user@example.com",
    avatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
)