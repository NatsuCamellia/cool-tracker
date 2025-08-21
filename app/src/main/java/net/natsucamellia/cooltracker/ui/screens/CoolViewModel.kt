package net.natsucamellia.cooltracker.ui.screens

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.Profile

class CoolViewModel(
    private val application: Application
) : ViewModel() {
    private val coolRepository = (application as CoolApplication).container.coolRepository
    var coolLoginState: CoolLoginState by mutableStateOf(CoolLoginState.Init)
        private set
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

    fun loadCourses(
        onDone: () -> Unit = {}
    ) {
        if (_coolUiState.value is CoolUiState.Error) {
            // Retry
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

    fun loadUserProfile(
        onDone: () -> Unit = {}
    ) {
        if (_accountUiState.value is AccountUiState.Error) {
            // Retry
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

    fun onLogin() {
        coolLoginState = CoolLoginState.LoggingIn
    }

    fun onLoggedIn(cookies: String) {
        coolRepository.saveUserSessionCookies(cookies)
        coolLoginState = CoolLoginState.LoggedIn
        postLogin()
    }

    private fun postLogin() {
        loadCourses()
        loadUserProfile()
    }

    fun logout() {
        coolRepository.clearUserSessionCookies()
        coolLoginState = CoolLoginState.LoggedOut
    }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        application.applicationContext.startActivity(intent)
    }

    sealed interface CoolLoginState {
        data object Init : CoolLoginState
        data object LoggedIn : CoolLoginState
        data object LoggedOut : CoolLoginState
        data object LoggingIn : CoolLoginState
    }

    sealed interface CoolUiState {
        data class Success(val courses: List<Course>) : CoolUiState
        data object Error : CoolUiState
        data object Loading : CoolUiState
    }

    sealed interface AccountUiState {
        data class Success(val profile: Profile) : AccountUiState
        data object Error : AccountUiState
        data object Loading : AccountUiState
    }
}