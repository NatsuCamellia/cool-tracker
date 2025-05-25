package net.natsucamellia.cooltracker.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

const val LOGIN_URL = "https://cool.ntu.edu.tw/login/"
const val BASE_URL = "https://cool.ntu.edu.tw/"
const val SUCCESS_URL_PREFIX = "https://cool.ntu.edu.tw/?login_success=1"


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebViewScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
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
                        if (url == BASE_URL || url != null && url.startsWith(SUCCESS_URL_PREFIX)) {
                            val cookieManager = CookieManager.getInstance()
                            val cookies: String = cookieManager.getCookie(url) // Get cookies for the current URL

                            Log.d("WebViewCookies", "Cookies for $url: $cookies")
                            // Important: Ensure onLoginSuccess is called on the main thread
                            // if it triggers UI changes or navigation outside of Compose.
                            // For Compose navigation, it's usually fine.
                            if (context is Activity) {
                                context.runOnUiThread {
                                    onLoginSuccess(cookies)
                                }
                            } else {
                                // Fallback or handle differently if not in an Activity context
                                // This is less common for full-screen composables
                                onLoginSuccess(cookies)
                            }
                        }
                    }

                    // Optional: You might want to override shouldOverrideUrlLoading
                    // if there are other types of redirects you want to handle within the WebView
                    // or open externally. For a simple login, onPageFinished is often enough.
                }
                settings.javaScriptEnabled = true // Enable JavaScript if required by the login page
                loadUrl(LOGIN_URL)
            }
        }
    )
}