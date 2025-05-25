package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import kotlin.time.Duration
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AssignmentScreen(
    coolViewModel: CoolViewModel
) {
    coolViewModel.coolUiState.collectAsState().value.let { coolUiState ->
        when (coolUiState) {
            is CoolViewModel.CoolUiState.Error -> ErrorScreen { coolViewModel.loadCourses() }
            is CoolViewModel.CoolUiState.Loading -> LoadingScreen { coolViewModel.loadCourses() }
            is CoolViewModel.CoolUiState.Success -> {
                val refreshState = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = coolViewModel.isRefreshing,
                    onRefresh = { coolViewModel.loadCourses() },
                    state = refreshState,
                    indicator = {
                        LoadingIndicator(
                            state = refreshState,
                            isRefreshing = coolViewModel.isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Ongoing",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        coolUiState.courses.forEach {
                            CourseCard(
                                course = it,
                                onGoing = true,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "Closed",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        coolUiState.courses.forEach {
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
    }


}

@Composable
fun CourseCard(
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            assignments.forEach{
                AssignmentCard(assignment = it, Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AssignmentCard(
    assignment: Assignment,
    modifier: Modifier = Modifier
) {
    val createdLocalDateTime = assignment.createdTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val dueLocalDateTime = assignment.dueTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val format = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth()
        char(' ')
        hour()
        char(':')
        minute()
    }

    val durationTotal = assignment.dueTime - assignment.createdTime
    val durationElapsed = Clock.System.now() - assignment.createdTime
    val durationRemaining = assignment.dueTime - Clock.System.now()
    val progress = durationElapsed.toDouble(DurationUnit.MINUTES) / durationTotal.toDouble(DurationUnit.MINUTES)

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = assignment.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (assignment.submissions.any { it.submitted }) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = "Status: Completed", // Add content description
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.primary // Use theme color
                    )
                }
                if (durationRemaining.isPositive()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop, // Or your preferred hourglass icon
                            contentDescription = "Time remaining",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.secondary // Use theme color
                        )
                        Text(
                            text = formatDurationLargestTwoUnits(durationRemaining),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearWavyProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    createdLocalDateTime.format(format),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    dueLocalDateTime.format(format),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    loadCourses: () -> Unit = {}
) {
    LaunchedEffect(true) { loadCourses() }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Retry Icon"
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Retry")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    val coolViewModel: CoolViewModel = viewModel()
    AssignmentScreen(coolViewModel)
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