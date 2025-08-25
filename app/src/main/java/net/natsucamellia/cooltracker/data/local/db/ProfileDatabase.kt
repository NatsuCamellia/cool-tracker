package net.natsucamellia.cooltracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.natsucamellia.cooltracker.model.Profile

@Database(
    entities = [Profile::class],
    version = 1
)

abstract class ProfileDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}