package net.natsucamellia.cooltracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import net.natsucamellia.cooltracker.nav.NavigationItem
import net.natsucamellia.cooltracker.ui.screens.AssignmentScreen
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.screens.LoginWebViewScreen

@Composable
fun COOLTrackerApp() {
    val coolViewModel: CoolViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<NavigationItem>(NavigationItem.Assignments) }
    val navigationItems = listOf(
        NavigationItem.Courses,
        NavigationItem.Assignments,
        NavigationItem.Grades
    )

    if (!coolViewModel.isLoggedIn) {
        Scaffold { innerPadding ->
            LoginWebViewScreen(modifier = Modifier.padding(innerPadding)) {
                coolViewModel.isLoggedIn = true
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    navigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentScreen == screen) screen.filledIcon else screen.outlinedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) { // Apply innerPadding to content
                when (currentScreen) {
                    NavigationItem.Assignments -> AssignmentScreen(coolViewModel)
                    NavigationItem.Courses -> PlaceholderScreen(title = "Courses Screen") // Placeholder
                    NavigationItem.Grades -> PlaceholderScreen(title = "Grades Screen")   // Placeholder
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "This is the $title")
    }
}