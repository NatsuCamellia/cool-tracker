package net.natsucamellia.cooltracker.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
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

class AuthManager(
    private val sharedPref: SharedPreferences,
    private val keystoreManager: KeystoreManager = KeystoreManager()
) {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState = _loginState.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkInitialStatus()
        }
    }

    fun login(cookie: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (validateCookie(cookie)) {
                ValidateResult.Valid -> {
                    _loginState.update { LoginState.LoggedIn(cookie) }
                    saveUserSessionCookies(cookie)
                }

                ValidateResult.Invalid -> {
                    _loginState.update { LoginState.LoggedOut }
                }

                ValidateResult.Disconnected -> {
                    _loginState.update { LoginState.Disconnected }
                }
            }
        }
    }

    fun logout() {
        // Clear the cookies from repository, since it's expected not to automatically login when
        // the app is started again. Also the user's intention is to clear the session, which is
        // the reason the user try to logout.
        _loginState.update { LoginState.LoggedOut }
        clearUserSessionCookies()
    }

    /** Save user's session cookies to local storage */
    private fun saveUserSessionCookies(cookies: String) {
        val encryptedPair = keystoreManager.encrypt(cookies)
        encryptedPair?.let { (encryptedCookies, iv) ->
            sharedPref.edit {
                putString(KEY_ENCRYPTED_COOKIES, encryptedCookies)
                // Also store the initialization vector (IV) for decryption
                putString(KEY_IV, iv)
                apply()
            }
            Log.d(TAG, "User session cookies saved successfully.")
        } ?: run {
            Log.e(TAG, "Failed to encrypt cookies.")
        }
    }

    /** Clear user's session cookies from local storage */
    private fun clearUserSessionCookies() {
        sharedPref.edit {
            remove(KEY_ENCRYPTED_COOKIES)
            remove(KEY_IV)
            apply()
        }
        Log.d(TAG, "User session cookies cleared successfully.")
    }

    /**
     * Try to login with user's session cookies in local storage.
     */
    suspend fun checkInitialStatus() {
        val encryptedData = sharedPref.getString(KEY_ENCRYPTED_COOKIES, null)
        val iv = sharedPref.getString(KEY_IV, null)

        if (encryptedData == null || iv == null) {
            _loginState.update { LoginState.LoggedOut }
            Log.d(TAG, "No stored user session cookies found.")
            return
        }

        val cookie = keystoreManager.decrypt(encryptedData, iv)
        if (cookie == null) {
            _loginState.update { LoginState.LoggedOut }
            Log.d(TAG, "Cannot decrypt cookies.")
            return
        }

        when (validateCookie(cookie)) {
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

    private suspend fun validateCookie(cookie: String): ValidateResult {
        // Try to load the profile page.
        // If the cookie is valid, the response will be successful.
        // Otherwise, the response will be a redirect.
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://cool.ntu.edu.tw/profile")
            .addHeader("Cookie", cookie)
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
        private const val KEY_ENCRYPTED_COOKIES = "encrypted_cool_cookies"
        private const val KEY_IV = "cool_cookies_iv"
    }

    sealed interface ValidateResult {
        object Valid : ValidateResult
        object Invalid : ValidateResult
        object Disconnected : ValidateResult
    }
}