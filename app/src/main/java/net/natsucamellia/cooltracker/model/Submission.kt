package net.natsucamellia.cooltracker.model

data class Submission(
    val id: Int,
    val submitted: Boolean
)

// Fake data
val fakeSubmission: Submission = Submission(
    12923670,
    submitted = false
)