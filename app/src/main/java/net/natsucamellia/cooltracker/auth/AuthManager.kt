package net.natsucamellia.cooltracker.auth

import android.util.Log
import android.webkit.CookieManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

sealed interface LoginState {
    data class LoggedIn(val cookies: String) : LoginState

    object LoggedOut : LoginState

    /** The session cookie exists, but can't be validated */
    object Disconnected : LoginState

    /** The auth manager is trying to recover session */
    object Loading : LoginState
}

class AuthManager() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState = _loginState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            refreshLoginState()
        }
    }

    /**
     * Log the user out.
     */
    fun logout() {
        // Clear the cookies in the CookieManager, since it's expected not to automatically login
        // when the app is started again. Also the user's intention is to clear the session, which
        // is the reason the user try to logout.
        _loginState.update { LoginState.LoggedOut }
        CookieManager.getInstance().removeAllCookies(null)
    }

    /**
     * Refresh the login state with user's session cookies in the CookieManager.
     */
    suspend fun refreshLoginState() {
        val cookie = CookieManager.getInstance().getCookie("https://cool.ntu.edu.tw/")
        if (cookie == null) {
            _loginState.update { LoginState.LoggedOut }
            Log.d(TAG, "Cannot decrypt cookies.")
            return
        }

        when (validateCookies(cookie)) {
            ValidateResult.Valid -> {
                _loginState.update { LoginState.LoggedIn(cookie) }
                Log.d(TAG, "User session cookies loaded successfully.")
            }

            ValidateResult.Invalid -> {
                _loginState.update { LoginState.LoggedOut }
                Log.d(TAG, "Invalid cookie")
            }

            ValidateResult.Disconnected -> {
                _loginState.update { LoginState.Disconnected }
                Log.d(TAG, "User is disconnected")
            }
        }
    }

    /**
     * Validate if the cookies contain a valid session.
     */
    private suspend fun validateCookies(cookies: String): ValidateResult {
        // Try to load the profile page.
        // If the cookie is valid, the response will be successful.
        // Otherwise, the response will be a redirect.
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://cool.ntu.edu.tw/profile")
            .addHeader("Cookie", cookies)
            .build()

        // Make sure the network connection runs on IO thread
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext ValidateResult.Valid
                    } else if (response.isRedirect) {
                        Log.d(TAG, "Invalid cookie")
                        return@withContext ValidateResult.Invalid
                    } else {
                        Log.d(TAG, "Request failed: ${response.code}")
                        return@withContext ValidateResult.Disconnected
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                return@withContext ValidateResult.Disconnected
            }
        }
    }

    companion object {
        private const val TAG = "AuthManager"
    }

    sealed interface ValidateResult {
        object Valid : ValidateResult
        object Invalid : ValidateResult
        object Disconnected : ValidateResult
    }
}