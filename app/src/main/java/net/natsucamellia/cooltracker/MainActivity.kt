package net.natsucamellia.cooltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import net.natsucamellia.cooltracker.ui.COOLTrackerApp
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.theme.COOLTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val coolViewModel = viewModel<CoolViewModel>(factory = CoolViewModel.Factory)
            COOLTrackerTheme {
                COOLTrackerApp(coolViewModel)
            }
        }
    }
}