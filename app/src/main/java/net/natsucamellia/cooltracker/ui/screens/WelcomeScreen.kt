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
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebViewScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding),
            factory = {
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            Log.d("WebViewCookies", "onPageStarted for $url")
                            if (url != null && url.startsWith(SUCCESS_URL_PREFIX)) {
                                val cookieManager = CookieManager.getInstance()
                                val cookies: String = cookieManager.getCookie(url)
                                onLoginSuccess(cookies)
                                cookieManager.removeAllCookies(null)
                            }
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl(LOGIN_URL)
                }
            }
        )
    }
}