package net.natsucamellia.cooltracker.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        // TODO: Use SQL to sort the assignments by due time.
        courseWithAssignmentsDao.getCoursesWithAssignments().map { coursesWithAssignments ->
            coursesWithAssignments.map { courseWithAssignments ->
                CourseWithAssignments(
                    course = courseWithAssignments.course,
                    assignments = courseWithAssignments.assignments.sortedBy { it.dueTime }
                )
            }
        }

    override suspend fun saveProfile(profile: Profile) {
        profileDao.insertProfile(profile)
    }

    override suspend fun saveCoursesWithAssignments(coursesWithAssignments: List<CourseWithAssignments>) {
        courseWithAssignmentsDao.insertCoursesWithAssignments(coursesWithAssignments)
    }

    override suspend fun clearAll() {
        profileDao.clearAll()
        courseWithAssignmentsDao.clearAllCourses()
        courseWithAssignmentsDao.clearAllAssignments()
    }
}