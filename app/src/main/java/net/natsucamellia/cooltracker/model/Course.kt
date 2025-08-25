package net.natsucamellia.cooltracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @sample [sampleCourse]
 */
@Entity(tableName = "course_table")
data class Course(
    @PrimaryKey
    val id: Int,
    val name: String,
    val isPublic: Boolean,
    val courseCode: String,
)

val Course.chineseName: String
    // Since the course code is of the format "$chineseName ($code)", we assume that $chineseName
    // does not contain any parentheses and thus can extract $chineseName by splitting on the
    // space before first parentheses.
    get() {
        val indexOfLeftParen = courseCode.indexOf('(')
        return name.substring(0, indexOfLeftParen - 1)
    }

val Course.englishName: String
    // Since the course code is of the format "$chineseName ($code)" and the name is of the format
    // "$chineseName $englishName", we use the same technique as above to extract $englishName.
    get() {
        val indexOfLeftParen = courseCode.indexOf('(')
        return name.substring(indexOfLeftParen).trim()
    }

val sampleCourse: Course = Course(
    id = 49109,
    name = "人工智慧導論 Foundations of Artificial Intelligence",
    isPublic = false,
    courseCode = "人工智慧導論 (CSIE3005-01、02)"
)