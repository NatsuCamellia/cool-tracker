package net.natsucamellia.cooltracker.auth

import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import android.webkit.CookieManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

sealed interface LoginState {
    /**
     * The user has logged in to NTU COOL.
     */
    data class LoggedIn(val cookies: String) : LoginState

    /**
     * The user has logged out from NTU COOL.
     */
    object LoggedOut : LoginState

    /** The session cookie exists, but can't be validated */
    object Disconnected : LoginState

    /** The auth manager is trying to recover session */
    object Loading : LoginState
}

class AuthManager(
    connectivityManager: ConnectivityManager
) {
    private val _loginStateEvent = MutableSharedFlow<LoginState>(replay = 1)
    val loginStateEvent = _loginStateEvent.asSharedFlow()

    // This callback is used to monitor system connectivity change
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available")
            super.onAvailable(network)
            // Refresh when the network is available
            CoroutineScope(Dispatchers.IO).launch {
                refreshLoginState()
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost")
            super.onLost(network)
            CoroutineScope(Dispatchers.IO).launch {
                _loginStateEvent.emit(LoginState.Disconnected)
            }
        }
    }

    init {
        // Try to recover user's login session
        CoroutineScope(Dispatchers.IO).launch {
            _loginStateEvent.emit(LoginState.Loading)
            refreshLoginState()
        }
        // Monitor system callback
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    /**
     * Log the user out.
     */
    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            // Clear the cookies in the CookieManager, since it's expected not to automatically login
            // when the app is started again. Also the user's intention is to clear the session, which
            // is the reason the user try to logout.
            CookieManager.getInstance().removeAllCookies(null)
            _loginStateEvent.emit(LoginState.LoggedOut)
        }
    }

    /**
     * Refresh the login state with user's session cookies in the CookieManager.
     */
    suspend fun refreshLoginState() {
        val cookie = CookieManager.getInstance().getCookie("https://cool.ntu.edu.tw/")
        if (cookie == null) {
            _loginStateEvent.emit(LoginState.LoggedOut)
            Log.d(TAG, "Cannot decrypt cookies.")
            return
        }

        when (validateCookies(cookie)) {
            ValidateResult.Valid -> {
                _loginStateEvent.emit(LoginState.LoggedIn(cookie))
                Log.d(TAG, "User session cookies loaded successfully.")
            }

            ValidateResult.Invalid -> {
                // TODO: Session timeout
                _loginStateEvent.emit(LoginState.LoggedOut)
                Log.d(TAG, "Invalid cookie")
            }

            ValidateResult.Disconnected -> {
                _loginStateEvent.emit(LoginState.Disconnected)
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
        // Otherwise, the response will be 401 (Unauthorized).
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://cool.ntu.edu.tw/api/v1/users/self/profile")
            .addHeader("Cookie", cookies)
            .build()

        // Make sure the network connection runs on IO thread
        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext ValidateResult.Valid
                    } else if (response.code == 401) {
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