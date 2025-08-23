package net.natsucamellia.cooltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import net.natsucamellia.cooltracker.ui.COOLTrackerApp
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.theme.COOLTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // https://www.reddit.com/r/androiddev/comments/1d6xk8h/completely_transparent_navigation_bar/
        // This prevent navigation bars (system) on some devices from being white or black, so they
        // can have the same color as the NavigationBar (the Composable).
        window.isNavigationBarContrastEnforced = false

        setContent {
            val coolViewModel = viewModel<CoolViewModel>(factory = CoolViewModel.Factory)
            COOLTrackerTheme {
                COOLTrackerApp(coolViewModel)
            }
        }
    }
}