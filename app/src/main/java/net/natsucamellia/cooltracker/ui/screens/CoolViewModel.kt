package net.natsucamellia.cooltracker.ui.screens

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
import net.natsucamellia.cooltracker.data.CoolRepository
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.Profile

class CoolViewModel(
    private val coolRepository: CoolRepository,
    private val authManager: AuthManager
) : ViewModel() {
    val loginState = authManager.loginState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        authManager.loginState.value
    )
    private val isLoading = MutableStateFlow(false)
    val coolUiState = combine(
        coolRepository.getActiveCoursesWithAssignments(),
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

    /**
     * Update the data from the repository.
     * Calls [onDone] after the data is updated.
     */
    fun updateData(
        onDone: () -> Unit = {}
    ) {
        isLoading.update { true }
        coolRepository.refresh(
            onDone = {
                isLoading.update { false }
                onDone()
            }
        )
    }

    /**
     * Try to log in to NTU COOL.
     */
    fun tryLogin() {
        viewModelScope.launch {
            authManager.refreshLoginState()
        }
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

    sealed interface CoolUiState {
        /**
         * There is data ready to be shown.
         * When refreshing, the state should be in [Success] since the data is already loaded.
         */
        data class Success(
            val coursesWithAssignments: List<CourseWithAssignments>,
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