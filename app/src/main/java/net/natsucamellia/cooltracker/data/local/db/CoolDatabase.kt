package net.natsucamellia.cooltracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile

@Database(
    entities = [
        Profile::class,
        Course::class,
        Assignment::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class CoolDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun courseWithAssignmentsDao(): CourseWithAssignmentsDao
}