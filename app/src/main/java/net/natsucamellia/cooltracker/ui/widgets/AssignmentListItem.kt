package net.natsucamellia.cooltracker.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.util.openUrl
import kotlin.time.Clock
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AssignmentListItem(
    assignment: Assignment,
    modifier: Modifier = Modifier
) {
    val createdLocalDateTime =
        assignment.createdTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val dueLocalDateTime =
        assignment.dueTime.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    val format = LocalDateTime.Format {
        monthNumber(padding = Padding.NONE)
        char('/')
        day()
        char(' ')
        hour()
        char(':')
        minute()
    }

    val durationTotal = assignment.dueTime - assignment.createdTime
    val durationElapsed = Clock.System.now() - assignment.createdTime
    val durationRemaining = assignment.dueTime - Clock.System.now()
    // Some assignments have due time earlier than created time
    val progress = if (durationTotal.isPositive())
        durationElapsed.toDouble(DurationUnit.MINUTES) / durationTotal.toDouble(DurationUnit.MINUTES)
    else 1.0
    val context = LocalContext.current

    ListItem(
        modifier = modifier.clickable {
            openUrl(context, assignment.htmlUrl)
        },
        headlineContent = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (assignment.submitted) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Submitted",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (progress < 1.0) {
                        Icon(
                            Icons.Rounded.HourglassTop,
                            contentDescription = "Unsubmitted",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Clear,
                            contentDescription = "Missing",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assignment.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearWavyProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

            }
        },
        supportingContent = {
            Row {
                Text(
                    createdLocalDateTime.format(format),
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dueLocalDateTime.format(format) +
                            if (durationRemaining.isPositive())
                                " (${assignment.remainingTimeOneUnit(context)})"
                            else
                                "",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}