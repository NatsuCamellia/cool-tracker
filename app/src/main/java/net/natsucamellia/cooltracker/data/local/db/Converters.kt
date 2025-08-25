package net.natsucamellia.cooltracker.data.local.db

import androidx.room.TypeConverter
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
}