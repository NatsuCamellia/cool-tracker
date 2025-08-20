package net.natsucamellia.cooltracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import net.natsucamellia.cooltracker.nav.NavigationItem
import net.natsucamellia.cooltracker.ui.screens.AccountScreen
import net.natsucamellia.cooltracker.ui.screens.AssignmentScreen
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.screens.CourseScreen
import net.natsucamellia.cooltracker.ui.screens.LoginWebViewScreen
import net.natsucamellia.cooltracker.ui.screens.WelcomeScreen

@Composable
fun COOLTrackerApp(
    coolViewModel: CoolViewModel
) {
    when (coolViewModel.coolLoginState) {
        CoolViewModel.CoolLoginState.Init -> {
            InitScreen()
        }

        CoolViewModel.CoolLoginState.LoggedOut -> {
            WelcomeScreen(
                onLogin = coolViewModel::onLogin
            )
        }

        CoolViewModel.CoolLoginState.LoggingIn -> {
            LoginWebViewScreen(
                onLoginSuccess = coolViewModel::onLoggedIn
            )
        }

        CoolViewModel.CoolLoginState.LoggedIn -> {
            LoggedInScreen(
                coolViewModel = coolViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InitScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
}

@Composable
fun LoggedInScreen(
    coolViewModel: CoolViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<NavigationItem>(NavigationItem.Assignments) }
    val navigationItems = listOf(
        NavigationItem.Courses,
        NavigationItem.Assignments,
        NavigationItem.Account
    )
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) { // Apply innerPadding to content
            when (currentScreen) {
                NavigationItem.Assignments -> AssignmentScreen(coolViewModel)
                NavigationItem.Courses -> CourseScreen(coolViewModel)
                NavigationItem.Account -> AccountScreen(coolViewModel)
            }
        }
    }
}