package net.natsucamellia.cooltracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlin.time.Instant

/**
 * @sample [sampleAssignment]
 */
@Entity(
    tableName = "assignment_table",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Assignment(
    @PrimaryKey
    val id: Int,
    val dueTime: Instant,
    val pointsPossible: Double,
    val createdTime: Instant,
    val name: String,
    val courseId: Int,
    val submitted: Boolean,
    val htmlUrl: String
)

val sampleAssignment: Assignment = Assignment(
    id = 309627,
    dueTime = Instant.parse("2026-06-11T15:59:59Z"),
    pointsPossible = 110.0,
    createdTime = Instant.parse("2025-05-14T06:10:50Z"),
    name = "Final Project",
    courseId = 49109,
    submitted = true,
    htmlUrl = "https://cool.ntu.edu.tw/"
)