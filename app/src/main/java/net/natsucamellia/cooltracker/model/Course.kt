package net.natsucamellia.cooltracker.model

data class Course(
    val id: Int,
    val name: String,
    val isPublic: Boolean,
    val courseCode: String,
    val assignments: List<Assignment>
)

val fakeCourse: Course = Course(
    id = 49109,
    name = "人工智慧導論 Foundations of Artificial Intelligence",
    isPublic = false,
    courseCode = "人工智慧導論 (CSIE3005-01、02)",
    assignments = listOf(fakeAssignment)
)