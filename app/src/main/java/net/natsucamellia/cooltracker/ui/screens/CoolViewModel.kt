package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.data.CoolRepository
import net.natsucamellia.cooltracker.model.Course

class CoolViewModel(
    private val coolRepository: CoolRepository
) : ViewModel() {
    var coolLoginState by mutableStateOf<CoolLoginState>(CoolLoginState.Init)
    private val _coolUiState = MutableStateFlow<CoolUiState>(CoolUiState.Loading)
    val coolUiState = _coolUiState.asStateFlow()

    init {
        viewModelScope.launch {
            coolLoginState = if (coolRepository.loadStoredUserSessionCookies()) {
                CoolLoginState.LoggedIn
            } else {
                CoolLoginState.LoggedOut
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

    fun logout() {
        coolRepository.clearUserSessionCookies()
        coolLoginState = CoolLoginState.LoggedOut
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as CoolApplication)
                val coolRepository = application.container.coolRepository
                CoolViewModel(coolRepository)
            }
        }
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
}