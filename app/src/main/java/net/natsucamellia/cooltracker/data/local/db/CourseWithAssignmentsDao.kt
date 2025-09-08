package net.natsucamellia.cooltracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.CourseWithAssignments

@Dao
interface CourseWithAssignmentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAssignments(assignment: List<Assignment>)

    @Transaction
    suspend fun insertCoursesWithAssignments(coursesWithAssignments: List<CourseWithAssignments>) {
        coursesWithAssignments.forEach {
            insertCourse(it.course)
            insertAllAssignments(it.assignments)
        }
    }

    @Transaction
    @Query("SELECT * FROM course_table")
    fun getCoursesWithAssignments(): Flow<List<CourseWithAssignments>>

    @Query("DELETE FROM course_table")
    suspend fun clearAllCourses()

    @Query("DELETE FROM assignment_table")
    suspend fun clearAllAssignments()
}