package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.fakeCourse

class CoolViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
        private set
    var coolUiState: CoolUiState by mutableStateOf(CoolUiState.Loading)
        private set

    init {
        getCourses()
    }

    private fun getCourses(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                isRefreshing = true
            }
            // Wait 3 seconds
            delay(3_000)
            coolUiState = CoolUiState.Success(listOf(fakeCourse))
//            coolUiState = CoolUiState.Error
            if (isRefresh) {
                isRefreshing = false
            }
        }
    }

    fun retry() {
        coolUiState = CoolUiState.Loading
        getCourses()
    }

    fun refresh() = getCourses(isRefresh = true)

    sealed interface CoolUiState {
        data class Success(val courses: List<Course>) : CoolUiState
        data object Error : CoolUiState
        data object Loading : CoolUiState
    }
}