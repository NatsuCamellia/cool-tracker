package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.auth.LoginState
import net.natsucamellia.cooltracker.data.CoolRepository
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile

class CoolViewModel(
    private val coolRepository: CoolRepository,
    private val authManager: AuthManager
) : ViewModel() {
    var coolLoginState: CoolLoginState by mutableStateOf(CoolLoginState.Init)
    private val isLoading = MutableStateFlow(false)
    val coolUiState = combine(
        coolRepository.getActiveCourses(),
        coolRepository.getUserProfile(),
        isLoading
    ) { courses, profile, isLoading ->
        if (isLoading && (courses == null || profile == null)) {
            // Only show loading when loading data for the first time
            // or when the data is still null.
            CoolUiState.Loading
        } else if (courses == null || profile == null) {
            CoolUiState.Error
        } else {
            CoolUiState.Success(courses, profile, isLoading)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CoolUiState.Loading
    )

    init {
        viewModelScope.launch {
            authManager.loginState.collect {
                when (it) {
                    is LoginState.LoggedIn -> {
                        coolLoginState = CoolLoginState.LoggedIn
                        postLogin()
                    }

                    is LoginState.LoggedOut -> {
                        coolLoginState = CoolLoginState.LoggedOut
                    }

                    else -> {
                        coolLoginState = CoolLoginState.Init
                    }
                }
            }
        }
    }

    /**
     * Update the data from the repository.
     * Calls [onDone] after the data is updated.
     */
    fun updateData(
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading.update { true }
            // TODO: Implement refresh after local source is implemented.
            isLoading.update { false }
            onDone()
        }
    }

    /**
     * Login to NTU COOL with the given [cookies].
     */
    fun login(cookies: String) {
        authManager.login(cookies)
    }

    /**
     * Do necessary works after login.
     */
    private fun postLogin() {
        updateData()
    }

    /**
     * Logout from NTU COOL.
     */
    fun logout() {
        authManager.logout()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Since the viewmodel need application to get app version, we pass the application
                // and it's context to the viewmodel. This won't cause any memory leaks because the
                // viewmodel will be destroyed when the activity is destroyed.
                val application = (this[APPLICATION_KEY] as CoolApplication)
                CoolViewModel(
                    coolRepository = application.container.coolRepository,
                    authManager = application.container.authManager
                )
            }
        }
    }

    sealed interface CoolLoginState {
        /** Waiting the viewmodel to initialize */
        data object Init : CoolLoginState
        data object LoggedIn : CoolLoginState
        data object LoggedOut : CoolLoginState
    }

    sealed interface CoolUiState {
        /**
         * There is data ready to be shown.
         * When refreshing, the state should be in [Success] since the data is already loaded.
         */
        data class Success(
            val courses: List<Course>,
            val profile: Profile,
            val isRefreshing: Boolean
        ) : CoolUiState

        data object Error : CoolUiState

        /**
         * Waiting the data to be loaded first time.
         * When refreshing, the state should be in [Success] since the data is already loaded.
         */
        data object Loading : CoolUiState
    }
}