package net.natsucamellia.cooltracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.model.Course
import net.natsucamellia.cooltracker.model.chineseName
import net.natsucamellia.cooltracker.model.englishName
import net.natsucamellia.cooltracker.ui.widgets.AssignmentListItem
import net.natsucamellia.cooltracker.ui.widgets.SectionLabel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
) {
    val refreshState = rememberPullToRefreshState()
    val courses = uiState.courses

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        state = refreshState,
        indicator = {
            LoadingIndicator(
                state = refreshState,
                isRefreshing = uiState.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // We use LazyColumn because the number of assignments of all courses may be large.
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssignmentDashboard(
                        uiState = uiState,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    SectionLabel(
                        text = stringResource(R.string.ongoing),
                    )
                }
            }
            items(courses) {
                CourseCard(
                    course = it,
                    onGoing = true,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StopCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    SectionLabel(
                        text = stringResource(R.string.closed)
                    )
                }
            }
            items(courses) {
                CourseCard(
                    course = it,
                    onGoing = false,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AssignmentDashboard(
    uiState: CoolViewModel.CoolUiState.Success,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now()
    val assignments = uiState.courses.flatMap { it.assignments }
    val onGoingAssignments = assignments.filter { it.dueTime > now }
    val pending = onGoingAssignments.count { !it.submitted }
    val onGoing = onGoingAssignments.size
    val submitted = assignments.count { it.submitted }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        DashboardItem(
            icon = Icons.Rounded.HourglassTop,
            clipShape = RoundedCornerShape(16.dp, 4.dp, 4.dp, 16.dp),
            text = pending.toString()
        )
        DashboardItem(
            icon = Icons.Outlined.PlayCircle,
            clipShape = RoundedCornerShape(4.dp),
            text = onGoing.toString()
        )
        DashboardItem(
            icon = Icons.Rounded.Check,
            clipShape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
            text = submitted.toString()
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardItem(
    icon: ImageVector,
    clipShape: Shape,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = clipShape
                )
                .size(96.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.displayLargeEmphasized,
                color = MaterialTheme.colorScheme.primary
            )
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
    // Filter assignment based on onGoing
    val assignments = course.assignments.filter {
        when (onGoing) {
            true -> it.dueTime > Clock.System.now()
            false -> it.dueTime <= Clock.System.now()
            null -> true
        }
    }

    if (assignments.isNotEmpty()) {
        Column(modifier = modifier) {
            // Chinese name and english name
            Text(
                course.chineseName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                course.englishName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Assignment list
            Column(
                modifier = Modifier.clip(MaterialTheme.shapes.large),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                assignments.forEach { assignment ->
                    AssignmentListItem(
                        assignment = assignment,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                    )
                }
            }
        }
    }
}

fun formatDurationLargestTwoUnits(context: Context, duration: Duration): String {
    return duration.toComponents { days, hours, minutes, seconds, _ ->
        val parts = mutableListOf<String>()
        if (days > 0) parts.add(context.getString(R.string.format_day_hour, days, hours))
        else if (hours > 0) parts.add(
            context.getString(
                R.string.format_hour_minute,
                hours,
                minutes
            )
        )
        else if (minutes > 0) parts.add(
            context.getString(
                R.string.format_minute_second,
                minutes,
                seconds
            )
        )
        else if (seconds > 0) parts.add(context.getString(R.string.format_second, seconds))
        if (parts.isEmpty()) {
            "0s"
        } else {
            parts.joinToString(" ")
        }
    }
}