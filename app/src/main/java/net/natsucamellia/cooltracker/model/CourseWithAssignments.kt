package net.natsucamellia.cooltracker.model

data class CourseWithAssignments(
    val course: Course,
    val assignments: List<Assignment>
)
