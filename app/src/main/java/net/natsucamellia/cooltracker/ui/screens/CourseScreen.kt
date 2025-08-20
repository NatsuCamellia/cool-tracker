package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.natsucamellia.cooltracker.model.Course

@Composable
fun CourseScreen(
    coolViewModel: CoolViewModel
) {
    coolViewModel.coolUiState.collectAsState().value.let { coolUiState ->
        when (coolUiState) {
            is CoolViewModel.CoolUiState.Error -> ErrorScreen { coolViewModel.loadCourses() }
            is CoolViewModel.CoolUiState.Loading -> LoadingScreen()
            is CoolViewModel.CoolUiState.Success -> SuccessScreen(coolUiState) { onDone ->
                coolViewModel.loadCourses(onDone = onDone)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SuccessScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit) -> Unit = {},
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "/", modifier = modifier) {
        composable("/") {
            CourseListScreen(
                uiState,
                onRefresh = onRefresh,
                onCourseClick = { navController.navigate("/$it") }
            )
        }
        composable(
            route = "/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) {
            val courseId = it.arguments?.getInt("courseId")
            val course = uiState.courses.find { course -> course.id == courseId }
            if (course != null) {
                CourseDetailScreen(course)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CourseListScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit) -> Unit = {},
    onCourseClick: (Int) -> Unit = {}
) {

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    val courses = uiState.courses
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh { isRefreshing = false }
        },
        state = refreshState,
        indicator = {
            LoadingIndicator(
                state = refreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = modifier
    ) {
        Column {
            if (!courses.isEmpty()) {
                Text(
                    "Ongoing",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                courses.forEach {
                    CourseListItem(
                        it,
                        modifier = Modifier.clickable(onClick = { onCourseClick(it.id) })
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseListItem(
    course: Course,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = course.name.replaceFirst(' ', '\n'),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        },
        modifier = modifier
    )
}