package net.natsucamellia.cooltracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.natsucamellia.cooltracker.model.Profile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("DELETE FROM profile_table")
    suspend fun clearAll()

    @Query("SELECT * FROM profile_table LIMIT 1")
    fun getProfile(): Flow<Profile?>
}