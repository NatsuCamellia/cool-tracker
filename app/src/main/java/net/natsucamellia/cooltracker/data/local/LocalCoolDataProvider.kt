package net.natsucamellia.cooltracker.data.local

import kotlinx.coroutines.flow.Flow
import net.natsucamellia.cooltracker.data.local.db.ProfileDao
import net.natsucamellia.cooltracker.model.Profile

interface LocalCoolDataProvider {
    fun getProfile(): Flow<Profile?>

    //    suspend fun getActiveCourses(): Flow<List<Course>?>
    suspend fun saveProfile(profile: Profile)

    //    suspend fun saveCourses(courses: List<Course>)
    suspend fun clearAll()
}

class LocalCoolDataProviderImpl(
    private val profileDao: ProfileDao
) : LocalCoolDataProvider {
    override fun getProfile(): Flow<Profile?> = profileDao.getProfile()

//    override suspend fun getActiveCourses(): Flow<List<Course>?> {
//        TODO("Not yet implemented")
//    }

    override suspend fun saveProfile(profile: Profile) {
        profileDao.insertProfile(profile)
    }

//    override suspend fun saveCourses(courses: List<Course>) {
//        TODO("Not yet implemented")
//    }

    override suspend fun clearAll() {
        profileDao.clearAll()
    }
}