package net.natsucamellia.cooltracker.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.auth.LoginState
import net.natsucamellia.cooltracker.data.local.LocalCoolDataProvider
import net.natsucamellia.cooltracker.data.remote.RemoteCoolDataProvider
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.Profile

interface CoolRepository {
    val isLoading: StateFlow<Boolean>

    /**
     * Get the current user's profile information from NTU COOL API.
     * @return user's profile information flow, null flow if failed.
     */
    fun getUserProfile(): Flow<Profile?>

    /**
     * Get the current user's active courses with assignments from NTU COOL API.
     * @return list of active courses flow, null flow if failed.
     */
    fun getActiveCoursesWithAssignments(): Flow<List<CourseWithAssignments>?>

    fun refresh(onDone: () -> Unit = {})
}

class NetworkCoolRepository(
    private val localCoolDataProvider: LocalCoolDataProvider,
    private val remoteCoolDataProvider: RemoteCoolDataProvider,
    private val authManager: AuthManager
) : CoolRepository {

    private val _isLoading = MutableStateFlow(false)
    override val isLoading = _isLoading.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            authManager.loginState.collect { state ->
                when (state) {
                    is LoginState.LoggedIn -> {
                        updateLocalDataWithRemoteData(state.cookies)
                    }

                    is LoginState.LoggedOut -> {
                        localCoolDataProvider.clearAll()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun getUserProfile(): Flow<Profile?> = localCoolDataProvider.getProfile()

    override fun getActiveCoursesWithAssignments(): Flow<List<CourseWithAssignments>?> =
        localCoolDataProvider.getCoursesWithAssignments()

    override fun refresh(onDone: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val state = authManager.loginState.value
            // Make sure the user is logged in before trying to refresh the data.
            authManager.refreshLoginState()
            val newState = authManager.loginState.value

            // We check both the old state and new state because if the user was not logged in
            // originally, the local data would be updated twice when the user logs in. One by this
            // method another by the init listener block.
            if (state is LoginState.LoggedIn && newState is LoginState.LoggedIn) {
                val cookies = newState.cookies
                // Try to update local data with remote data
                updateLocalDataWithRemoteData(cookies)
            }
            onDone()
        }
    }

    private suspend fun updateLocalDataWithRemoteData(cookies: String) {
        _isLoading.update { true }
        remoteCoolDataProvider.getUserProfile(cookies)?.let {
            localCoolDataProvider.saveProfile(it)
        }
        remoteCoolDataProvider.getActiveCoursesWithAssignments(cookies)?.let {
            localCoolDataProvider.saveCoursesWithAssignments(it)
        }
        _isLoading.update { false }
    }
}