package net.natsucamellia.cooltracker.model

import android.content.Context
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import net.natsucamellia.cooltracker.R
import kotlin.time.Clock
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
) {
    fun remainingTimeOneUnit(context: Context): String {
        val durationRemaining = dueTime - Clock.System.now()
        if (durationRemaining.isNegative()) return "0s"
        return durationRemaining.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) context.getString(R.string.format_day, days)
            else if (hours > 0) context.getString(R.string.format_hour, hours)
            else if (minutes > 0) context.getString(R.string.format_minute, minutes)
            else context.getString(R.string.format_second, seconds)
        }
    }
}

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