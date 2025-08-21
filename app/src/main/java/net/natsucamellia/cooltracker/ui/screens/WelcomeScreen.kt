package net.natsucamellia.cooltracker.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome")
            Button(
                onClick = onLogin
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, "Login")
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text("Login")
            }
        }
    }
}

const val LOGIN_URL = "https://cool.ntu.edu.tw/login/"
const val SUCCESS_URL_PREFIX = "https://cool.ntu.edu.tw/?login_success=1"

/**
 * Login to NTU COOL in a WebView screen.
 * [onLoginSuccess] will be called with the session cookies when the user logs in.
 *
 * @param onLoginSuccess The callback to be called when the user has logged in successfully. The
 * session cookies will be passed to this callback.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebViewScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit
) {
    // I'm not sure if we can obtain session cookies from external browsers, which is preferred,
    // but we use embedded WebView for now. If you know how to do it, please try.
    val context = LocalContext.current

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = {
                // Create a WebView for the login page
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        // This function is called when a page start loading. We choose this instead
                        // of the others because we want to return to the app as soon as the user
                        // has logged in and been redirected to the homepage, whose url starts with
                        // SUCCESS_URL_PREFIX.
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewCookies", "onPageStarted for $url")
                            if (url != null && url.startsWith(SUCCESS_URL_PREFIX)) {
                                // The user has logged and been redirected to the homepage.
                                // Now we can obtain and pass the cookies to the callback and return
                                // to the app.
                                val cookieManager = CookieManager.getInstance()
                                val cookies: String = cookieManager.getCookie(url)
                                onLoginSuccess(cookies)
                                // Clear the cookies for privacy and security.
                                cookieManager.removeAllCookies(null)
                            }
                        }
                    }
                    // Javascript is needed to login, and it seems to be dangerous because the
                    // linter complains about it. We add a annotation at the top of this composable
                    // for now, but we should avoid it in the future.
                    settings.javaScriptEnabled = true
                    loadUrl(LOGIN_URL)
                }
            }
        )
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    WelcomeScreen()
}