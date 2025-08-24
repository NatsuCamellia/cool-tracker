package net.natsucamellia.cooltracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.recalculateWindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import net.natsucamellia.cooltracker.nav.CoolNavigationDestination
import net.natsucamellia.cooltracker.nav.CoolNavigationWrapper
import net.natsucamellia.cooltracker.nav.Route
import net.natsucamellia.cooltracker.ui.screens.AccountScreen
import net.natsucamellia.cooltracker.ui.screens.AssignmentScreen
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.screens.CourseScreen
import net.natsucamellia.cooltracker.ui.screens.ErrorScreen
import net.natsucamellia.cooltracker.ui.screens.LoadingScreen
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
                onLogin = coolViewModel::onLoginClicked
            )
        }

        CoolViewModel.CoolLoginState.LoggingIn -> {
            LoginWebViewScreen(
                onLoginSuccess = coolViewModel::login
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
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
    ) { innerPadding ->
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoggedInScreen(
    coolViewModel: CoolViewModel
) {
    val uiState = coolViewModel.coolUiState.collectAsState().value
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    when (uiState) {
        is CoolViewModel.CoolUiState.Error -> {
            ErrorScreen(onRetry = coolViewModel::updateData)
        }

        is CoolViewModel.CoolUiState.Loading -> {
            LoadingScreen()
        }

        is CoolViewModel.CoolUiState.Success -> {
            CoolNavigationWrapper(
                currentDestination = currentDestination,
                navigateToTopLevelDestination = { coolDestination ->
                    navController.navigate(coolDestination.route)
                },
                modifier = Modifier.recalculateWindowInsets()
            ) {
                CoolNavHost(
                    navController = navController,
                    uiState = uiState,
                    refresh = coolViewModel::updateData,
                    logout = coolViewModel::logout,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
            }
        }
    }
}

@Composable
private fun CoolNavHost(
    navController: NavHostController,
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    refresh: () -> Unit = {},
    logout: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = CoolNavigationDestination.Courses.route,
        modifier = modifier
    ) {
        composable<Route.Courses> {
            CourseScreen(
                uiState = uiState
            )
        }
        composable<Route.Assignments> {
            AssignmentScreen(
                uiState = uiState, onRefresh = refresh
            )
        }
        composable<Route.Account> {
            AccountScreen(
                uiState = uiState, logout = logout
            )
        }
    }
}
