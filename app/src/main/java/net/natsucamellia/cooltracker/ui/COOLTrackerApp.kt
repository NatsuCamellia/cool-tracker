package net.natsucamellia.cooltracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
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
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.nav.NavigationItem
import net.natsucamellia.cooltracker.ui.screens.AccountScreen
import net.natsucamellia.cooltracker.ui.screens.AssignmentScreen
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.screens.LoginWebViewScreen
import net.natsucamellia.cooltracker.ui.screens.WelcomeScreen

@Composable
fun COOLTrackerApp(
    coolApplication: CoolApplication
) {
    val coolViewModel: CoolViewModel = viewModel(factory = CoolViewModel.Factory)
    var currentScreen by remember { mutableStateOf<NavigationItem>(NavigationItem.Assignments) }
    val navigationItems = listOf(
        NavigationItem.Courses,
        NavigationItem.Assignments,
        NavigationItem.Grades,
        NavigationItem.Account
    )

    when (coolViewModel.coolLoginState) {
        CoolViewModel.CoolLoginState.Init -> {
            InitScreen()
        }
        CoolViewModel.CoolLoginState.LoggedOut -> {
            WelcomeScreen(
                onLogin = {
                    coolViewModel.coolLoginState = CoolViewModel.CoolLoginState.LoggingIn
                }
            )
        }
        CoolViewModel.CoolLoginState.LoggingIn -> {
            LoginWebViewScreen { cookies ->
                coolApplication.container.coolRepository.saveUserSessionCookies(cookies)
                coolViewModel.coolLoginState = CoolViewModel.CoolLoginState.LoggedIn
            }
        }
        CoolViewModel.CoolLoginState.LoggedIn -> {
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
                        NavigationItem.Account -> AccountScreen(coolViewModel)
                    }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InitScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
}