package net.natsucamellia.cooltracker.model

import kotlinx.datetime.Instant

data class Assignment(
    val id: Int,
    val dueTime: Instant,
    val pointsPossible: Double,
    val createdTime: Instant,
    val name: String,
    val submissions: List<Submission>
)

val fakeAssignment: Assignment = Assignment(
    id = 309627,
    dueTime = Instant.parse("2025-06-11T15:59:59Z"),
    pointsPossible = 110.0,
    createdTime = Instant.parse("2025-05-14T06:10:50Z"),
    name = "Final Project",
    submissions = listOf(fakeSubmission)
)