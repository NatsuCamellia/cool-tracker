package net.natsucamellia.cooltracker.model

import androidx.room.Embedded
import androidx.room.Relation

data class CourseWithAssignments(
    @Embedded
    val course: Course,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val assignments: List<Assignment>
)
