package net.natsucamellia.cooltracker.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.auth.LoginState
import net.natsucamellia.cooltracker.data.local.LocalCoolDataProvider
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.Profile

interface CoolRepository {
    /**
     * Get the current user's profile information.
     * @return user's profile information, null if failed.
     */
    fun getUserProfile(): Flow<Profile?>

    /**
     * Get the current user's active courses.
     * @return list of active courses, null if failed.
     */
    fun getActiveCoursesWithAssignments(): Flow<List<CourseWithAssignments>?>
}

class NetworkCoolRepository(
    private val localCoolDataProvider: LocalCoolDataProvider,
    private val remoteCoolDataProvider: RemoteCoolDataProvider,
    private val authManager: AuthManager
) : CoolRepository {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collect { state ->
                when (state) {
                    is LoginState.LoggedIn -> {
                        val profile = remoteCoolDataProvider.getUserProfile(state.cookies)
                        if (profile != null) {
                            localCoolDataProvider.saveProfile(profile)
                        }
                    }

                    is LoginState.LoggedOut -> {
                        localCoolDataProvider.clearAll()
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserProfile(): Flow<Profile?> = localCoolDataProvider.getProfile()

    /**
     * Get the current user's active courses from NTU COOL API.
     * @return list of active courses flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveCoursesWithAssignments(): Flow<List<CourseWithAssignments>?> =
        authManager.loginState.flatMapLatest { state ->
            when (state) {
                is LoginState.LoggedIn -> {
                    val cookies = state.cookies
                    val courses = remoteCoolDataProvider.getActiveCoursesWithAssignments(cookies)
                    flowOf(courses)
                }

                else -> {
                    flowOf(null)
                }
            }
        }

}