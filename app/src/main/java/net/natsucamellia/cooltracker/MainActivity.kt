package net.natsucamellia.cooltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.natsucamellia.cooltracker.ui.COOLTrackerApp
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.theme.COOLTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Since the viewmodel need a context to make intents, we pass the application and it's
        // context to the viewmodel. This won't cause any memory leaks because the viewmodel
        // will be destroyed when the activity is destroyed.
        val coolViewModel = CoolViewModel(application)
        setContent {
            COOLTrackerTheme {
                COOLTrackerApp(coolViewModel)
            }
        }
    }
}