package net.natsucamellia.cooltracker.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * @sample [sampleCourseWithAssignments]
 */
data class CourseWithAssignments(
    @Embedded
    val course: Course,
    @Relation(
        parentColumn = "id",
        entityColumn = "course_id"
    )
    val assignments: List<Assignment>
)

val sampleCourseWithAssignments = CourseWithAssignments(
    course = sampleCourse,
    assignments = listOf(sampleAssignment)
)