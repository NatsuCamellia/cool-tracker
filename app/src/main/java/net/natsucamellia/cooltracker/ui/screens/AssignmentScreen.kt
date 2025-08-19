package net.natsucamellia.cooltracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@Composable
fun AssignmentScreen(
    coolViewModel: CoolViewModel
) {
    coolViewModel.coolUiState.collectAsState().value.let { coolUiState ->
        when (coolUiState) {
            is CoolViewModel.CoolUiState.Error -> ErrorScreen { coolViewModel.loadCourses() }
            is CoolViewModel.CoolUiState.Loading -> LoadingScreen { coolViewModel.loadCourses() }
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
fun SuccessScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit) -> Unit = {},
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
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
            Text(
                "Ongoing",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            if (uiState.courses.any { course -> course.assignments.any { it.dueTime > Clock.System.now() } }) {
                uiState.courses.forEach {
                    CourseCard(
                        course = it,
                        onGoing = true,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                Text(
                    "No ongoing assignments",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Closed",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            if (uiState.courses.any { course -> course.assignments.any { it.dueTime <= Clock.System.now() } }) {
                uiState.courses.forEach {
                    CourseCard(
                        course = it,
                        onGoing = false,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                Text(
                    "No closed assignments",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
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
            assignments.forEach {
                AssignmentCard(assignment = it, Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun AssignmentCard(
    assignment: Assignment,
    modifier: Modifier = Modifier
) {
    val createdLocalDateTime =
        assignment.createdTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val dueLocalDateTime =
        assignment.dueTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val format = LocalDateTime.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day()
        char(' ')
        hour()
        char(':')
        minute()
    }

    val durationTotal = assignment.dueTime - assignment.createdTime
    val durationElapsed = Clock.System.now() - assignment.createdTime
    val durationRemaining = assignment.dueTime - Clock.System.now()
    val progress =
        durationElapsed.toDouble(DurationUnit.MINUTES) / durationTotal.toDouble(DurationUnit.MINUTES)

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