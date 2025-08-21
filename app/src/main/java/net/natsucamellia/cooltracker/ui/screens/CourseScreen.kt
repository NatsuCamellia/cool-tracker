package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.chineseName
import net.natsucamellia.cooltracker.model.englishName
import net.natsucamellia.cooltracker.ui.theme.ClipShapes
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel

@Composable
fun CourseScreen(
    coolViewModel: CoolViewModel,
) {
    coolViewModel.coolUiState.collectAsState().value.let { coolUiState ->
        when (coolUiState) {
            is CoolViewModel.CoolUiState.Error -> ErrorScreen { coolViewModel.loadCourses() }
            is CoolViewModel.CoolUiState.Loading -> LoadingScreen()
            is CoolViewModel.CoolUiState.Success -> SuccessScreen(
                uiState = coolUiState,
            ) { onDone ->
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
                uiState = uiState,
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
                CourseDetailScreen(
                    course = course,
                    navigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
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
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel(
                text = "Ongoing",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(ClipShapes.outerRoundedCornerShape)
            ) {
                courses.forEach {
                    CourseListItem(
                        course = it,
                        modifier = Modifier
                            .clickable(
                                onClick = { onCourseClick(it.id) }
                            )
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
                text = course.chineseName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = course.englishName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier
    )
}