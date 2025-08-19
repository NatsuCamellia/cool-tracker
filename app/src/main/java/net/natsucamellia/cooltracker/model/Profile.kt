package net.natsucamellia.cooltracker.model

data class Profile(
    val id: Int,
    val name: String,
    val bio: String?,
    val primaryEmail: String,
    val avatarUrl: String?
)

val fakeProfile: Profile = Profile(
    id = 1,
    name = "Sample User",
    bio = "Hello!",
    primaryEmail = "sample_user@example.com",
    avatarUrl = null
)