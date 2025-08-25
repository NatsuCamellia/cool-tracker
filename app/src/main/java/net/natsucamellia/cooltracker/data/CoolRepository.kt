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
import net.natsucamellia.cooltracker.model.Course
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
    fun getActiveCourses(): Flow<List<Course>?>
}

class NetworkCoolRepository(
    private val remoteCoolDataProvider: RemoteCoolDataProvider, private val authManager: AuthManager
) : CoolRepository {
    private var userSessionCookies: String? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collect {
                userSessionCookies = if (it is LoginState.LoggedIn) {
                    it.cookies
                } else {
                    null
                }
            }
        }
    }

    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserProfile(): Flow<Profile?> = authManager.loginState.flatMapLatest { state ->
        when (state) {
            is LoginState.LoggedIn -> {
                val cookies = state.cookies
                val profile = remoteCoolDataProvider.getUserProfile(cookies)
                flowOf(profile)
            }

            else -> {
                flowOf(null)
            }
        }
    }

    /**
     * Get the current user's active courses from NTU COOL API.
     * @return list of active courses flow, null flow if failed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveCourses(): Flow<List<Course>?> =
        authManager.loginState.flatMapLatest { state ->
            when (state) {
                is LoginState.LoggedIn -> {
                    val cookies = state.cookies
                    val courses = remoteCoolDataProvider.getActiveCourses(cookies)
                    flowOf(courses)
                }

                else -> {
                    flowOf(null)
                }
            }
        }

}