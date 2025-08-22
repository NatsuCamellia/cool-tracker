package net.natsucamellia.cooltracker.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile
import net.natsucamellia.cooltracker.nav.NavigationItem

class CoolViewModel(
    private val application: Application
) : ViewModel() {
    private val coolRepository = (application as CoolApplication).container.coolRepository

    // There is not specific reason to choose Compose state or StateFlow, the following states
    // are chosen at random. They can be changed if needed.
    var coolLoginState: CoolLoginState by mutableStateOf(CoolLoginState.Init)
        private set
    var currentScreen: NavigationItem by mutableStateOf(NavigationItem.Assignments)
    private val _coolUiState = MutableStateFlow<CoolUiState>(CoolUiState.Loading)
    val coolUiState = _coolUiState.asStateFlow()
    private val _accountUiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val accountUiState = _accountUiState.asStateFlow()

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
     * Load the courses from repository and calls [onDone] when done.
     */
    fun loadCourses(
        onDone: () -> Unit = {}
    ) {
        if (_coolUiState.value is CoolUiState.Error) {
            // Since there is no data to be shown, set to Loading.
            _coolUiState.value = CoolUiState.Loading
        }

        viewModelScope.launch {
            val courses = coolRepository.getActiveCourses()
            if (courses == null) {
                _coolUiState.value = CoolUiState.Error
            } else {
                _coolUiState.value = CoolUiState.Success(courses)
            }
            onDone()
        }
    }

    /**
     * Load the current user's profile from repository and calls [onDone] when done.
     */
    fun loadUserProfile(
        onDone: () -> Unit = {}
    ) {
        if (_accountUiState.value is AccountUiState.Error) {
            // Since there is no data to be shown, set to Loading.
            _accountUiState.value = AccountUiState.Loading
        }

        viewModelScope.launch {
            val profile = coolRepository.getUserProfile()
            if (profile == null) {
                _accountUiState.value = AccountUiState.Error
            } else {
                _accountUiState.value = AccountUiState.Success(profile)
            }
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
        loadCourses()
        loadUserProfile()
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

    /**
     * Open the given [url] in the external browser.
     */
    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getAppVersion(): String {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        return "${packageInfo.versionName} (${packageInfo.longVersionCode})"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Since the viewmodel need application to get app version, we pass the application
                // and it's context to the viewmodel. This won't cause any memory leaks because the
                // viewmodel will be destroyed when the activity is destroyed.
                val application = (this[APPLICATION_KEY] as CoolApplication)
                CoolViewModel(application)
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
        data class Success(val courses: List<Course>) : CoolUiState
        data object Error : CoolUiState

        /**
         * Waiting the data to be loaded first time.
         * When refreshing, the state should be in [Success] since the data is already loaded.
         */
        data object Loading : CoolUiState
    }

    sealed interface AccountUiState {
        data class Success(val profile: Profile) : AccountUiState
        data object Error : AccountUiState
        data object Loading : AccountUiState
    }
}