package net.natsucamellia.cooltracker.data.local

import kotlinx.coroutines.flow.Flow
import net.natsucamellia.cooltracker.data.local.db.CourseWithAssignmentsDao
import net.natsucamellia.cooltracker.data.local.db.ProfileDao
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.Profile

interface LocalCoolDataProvider {
    fun getProfile(): Flow<Profile?>

    fun getCoursesWithAssignments(): Flow<List<CourseWithAssignments>>
    suspend fun saveProfile(profile: Profile)

    suspend fun saveCoursesWithAssignments(coursesWithAssignments: List<CourseWithAssignments>)
    suspend fun clearAll()
}

class LocalCoolDataProviderImpl(
    private val profileDao: ProfileDao,
    private val courseWithAssignmentsDao: CourseWithAssignmentsDao
) : LocalCoolDataProvider {
    override fun getProfile(): Flow<Profile?> = profileDao.getProfile()

    override fun getCoursesWithAssignments(): Flow<List<CourseWithAssignments>> =
        courseWithAssignmentsDao.getCoursesWithAssignments()

    override suspend fun saveProfile(profile: Profile) {
        profileDao.insertProfile(profile)
    }

    override suspend fun saveCoursesWithAssignments(coursesWithAssignments: List<CourseWithAssignments>) {
        coursesWithAssignments.forEach {
            courseWithAssignmentsDao.insertCourseWithAssignments(it)
        }
    }

    override suspend fun clearAll() {
        profileDao.clearAll()
        courseWithAssignmentsDao.clearAllCourses()
        courseWithAssignmentsDao.clearAllAssignments()
    }
}