package net.natsucamellia.cooltracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun AssignmentScreen(
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
                SectionLabel(
                    text = stringResource(R.string.ongoing),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
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
                SectionLabel(
                    text = stringResource(R.string.closed),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
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