package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.ui.widgets.AssignmentListItem
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Composable
fun AssignmentScreen(
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

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalTime::class
)
@Composable
private fun SuccessScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit) -> Unit = {},
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val courses = uiState.courses

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Assignments",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
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
            modifier = modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Ongoing",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                courses.forEach {
                    CourseCard(
                        course = it,
                        onGoing = true,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Closed",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                courses.forEach {
                    CourseCard(
                        course = it,
                        onGoing = false,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onGoing: Boolean? = null
) {
    val assignments = course.assignments.filter {
        when (onGoing) {
            true -> it.dueTime > Clock.System.now()
            false -> it.dueTime <= Clock.System.now()
            null -> true
        }
    }

    if (assignments.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                course.name.replaceFirst(' ', '\n'),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            assignments.forEachIndexed { index, assignment ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )

                    course.assignments.lastIndex -> RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )

                    else -> RoundedCornerShape(8.dp)
                }
                AssignmentListItem(
                    assignment = assignment,
                    Modifier
                        .padding(vertical = 1.dp)
                        .clip(shape)
                )
            }
        }
    }
}

fun formatDurationLargestTwoUnits(duration: Duration): String {
    return duration.toComponents { days, hours, minutes, seconds, _ ->
        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d ${hours}h")
        else if (hours > 0) parts.add("${hours}h ${minutes}m")
        else if (minutes > 0) parts.add("${minutes}m ${seconds}s")
        else if (seconds > 0) parts.add("${seconds}s")
        if (parts.isEmpty()) {
            "0s"
        } else {
            parts.joinToString(" ")
        }
    }
}