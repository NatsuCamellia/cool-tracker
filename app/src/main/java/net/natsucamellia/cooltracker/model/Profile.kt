package net.natsucamellia.cooltracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @sample [sampleProfile]
 */
@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey
    @ColumnInfo("id")
    val id: Int,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("bio")
    val bio: String?,
    @ColumnInfo("primary_email")
    val primaryEmail: String,
    @ColumnInfo("avatar_url")
    val avatarUrl: String
)

val sampleProfile: Profile = Profile(
    id = 1,
    name = "Sample User",
    bio = "Hello!",
    primaryEmail = "sample_user@example.com",
    avatarUrl = "https://cool.ntu.edu.tw/images/messages/avatar-50.png"
)