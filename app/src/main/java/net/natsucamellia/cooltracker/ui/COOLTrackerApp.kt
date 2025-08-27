package net.natsucamellia.cooltracker.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowSizeClass
import net.natsucamellia.cooltracker.auth.LoginState
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.nav.CoolNavigationDestination
import net.natsucamellia.cooltracker.nav.CoolNavigationSuiteScaffold
import net.natsucamellia.cooltracker.nav.Route
import net.natsucamellia.cooltracker.ui.screens.AccountScreen
import net.natsucamellia.cooltracker.ui.screens.CoolViewModel
import net.natsucamellia.cooltracker.ui.screens.CourseDetailScreen
import net.natsucamellia.cooltracker.ui.screens.CourseListScreen
import net.natsucamellia.cooltracker.ui.screens.ErrorScreen
import net.natsucamellia.cooltracker.ui.screens.HomeScreen
import net.natsucamellia.cooltracker.ui.screens.LoadingScreen
import net.natsucamellia.cooltracker.ui.screens.TwoPaneCourseView
import net.natsucamellia.cooltracker.ui.screens.WelcomeScreen

@Composable
fun COOLTrackerApp(
    coolViewModel: CoolViewModel
) {
    when (coolViewModel.loginState.collectAsState().value) {
        LoginState.Loading -> {
            InitScreen()
        }

        LoginState.LoggedOut -> {
            WelcomeScreen(
                onLoginSuccess = coolViewModel::tryLogin
            )
        }

        is LoginState.LoggedIn, LoginState.Disconnected -> {
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

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        when (uiState) {
            is CoolViewModel.CoolUiState.Error -> {
                ErrorScreen(onRetry = coolViewModel::updateData)
            }

            is CoolViewModel.CoolUiState.Loading -> {
                LoadingScreen()
            }

            is CoolViewModel.CoolUiState.Success -> {
                CoolNavigationSuiteScaffold(
                    currentDestination = currentDestination,
                    navigateToTopLevelDestination = { coolDestination ->
                        navController.navigate(coolDestination.route)
                    },
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
}

@Composable
private fun CoolNavHost(
    navController: NavHostController,
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    refresh: () -> Unit = {},
    logout: () -> Unit = {}
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    NavHost(
        navController = navController,
        startDestination = CoolNavigationDestination.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(
                animationSpec = tween(200)
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(200)
            )
        }
    ) {
        val coursesWithAssignments = uiState.coursesWithAssignments
        val profile = uiState.profile
        val isRefreshing = uiState.isRefreshing
        composable<Route.Courses> { backStackEntry ->
            val destination: Route.Courses = backStackEntry.toRoute()
            val courseId = destination.courseId
            val onCourseClick: (Course) -> Unit = { course ->
                navController.navigate(Route.Courses(courseId = course.id))
            }

            if (twoPane) {
                TwoPaneCourseView(
                    coursesWithAssignments = coursesWithAssignments,
                    courseId = courseId,
                    onCourseClick = onCourseClick,
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                if (courseId == null) {
                    // Only course list
                    CourseListScreen(
                        coursesWithAssignments = coursesWithAssignments,
                        onCourseClick = onCourseClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    // Only course detail
                    val courseWithAssignments =
                        coursesWithAssignments.find { cwa -> cwa.course.id == courseId }
                    // Ideally this should never be null
                    if (courseWithAssignments != null) {
                        CourseDetailScreen(
                            courseWithAssignments = courseWithAssignments,
                            navigateUp = { navController.navigateUp() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
        composable<Route.Assignments> {
            HomeScreen(
                coursesWithAssignments = coursesWithAssignments,
                isRefreshing = isRefreshing,
                onRefresh = refresh
            )
        }
        composable<Route.Account> {
            AccountScreen(
                profile = profile,
                logout = logout
            )
        }
    }
}
