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
import net.natsucamellia.cooltracker.data.CoolRepository
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile
import net.natsucamellia.cooltracker.nav.NavigationItem

class CoolViewModel(
    private val coolRepository: CoolRepository,
) : ViewModel() {
    // There is not specific reason to choose Compose state or StateFlow, the following states
    // are chosen at random. They can be changed if needed.
    var coolLoginState: CoolLoginState by mutableStateOf(CoolLoginState.Init)
        private set
    var currentScreen: NavigationItem by mutableStateOf(NavigationItem.Assignments)
    private val courses = MutableStateFlow<List<Course>?>(null)
    private val profile = MutableStateFlow<Profile?>(null)
    private val isLoading = MutableStateFlow<Boolean>(false)
    val coolUiState = combine(courses, profile, isLoading) { courses, profile, isLoading ->
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
        // Try to restore session
        viewModelScope.launch {
            coolLoginState = if (coolRepository.loadStoredUserSessionCookies()) {
                CoolLoginState.LoggedIn
            } else {
                CoolLoginState.LoggedOut
            }

            if (coolLoginState == CoolLoginState.LoggedIn) {
                postLogin()
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
            courses.update { coolRepository.getActiveCourses() }
            profile.update { coolRepository.getUserProfile() }
            isLoading.update { false }
            onDone()
        }
    }

    /**
     * Handle the login button click.
     */
    fun onLoginClicked() {
        coolLoginState = CoolLoginState.LoggingIn
    }

    /**
     * Login to NTU COOL with the given [cookies].
     */
    fun login(cookies: String) {
        // TODO: Check if the cookies are valid
        coolRepository.saveUserSessionCookies(cookies)
        coolLoginState = CoolLoginState.LoggedIn
        postLogin()
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
        // Clear the cookies from repository, since it's expected not to automatically login when
        // the app is started again. Also the user's intention is to clear the session, which is
        // the reason the user try to logout.
        coolRepository.clearUserSessionCookies()
        coolLoginState = CoolLoginState.LoggedOut
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Since the viewmodel need application to get app version, we pass the application
                // and it's context to the viewmodel. This won't cause any memory leaks because the
                // viewmodel will be destroyed when the activity is destroyed.
                val application = (this[APPLICATION_KEY] as CoolApplication)
                CoolViewModel(application.container.coolRepository)
            }
        }
    }

    sealed interface CoolLoginState {
        /** Waiting the viewmodel to initialize */
        data object Init : CoolLoginState
        data object LoggedIn : CoolLoginState
        data object LoggedOut : CoolLoginState

        /** Waiting the user to login into NTU COOL in the WebView */
        data object LoggingIn : CoolLoginState
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