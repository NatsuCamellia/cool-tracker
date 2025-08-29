package net.natsucamellia.cooltracker.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import net.natsucamellia.cooltracker.CoolApplication
import net.natsucamellia.cooltracker.MainActivity
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.CourseWithAssignments
import net.natsucamellia.cooltracker.model.chineseName
import net.natsucamellia.cooltracker.model.sampleAssignment
import net.natsucamellia.cooltracker.model.sampleCourse
import net.natsucamellia.cooltracker.ui.widgets.openUrlIntent
import kotlin.time.Clock

class AssignmentsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AssignmentsWidget()
}

class AssignmentsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val application: CoolApplication = context as CoolApplication
        val coolRepository = application.container.coolRepository
        provideContent {
            val coursesWithAssignments = coolRepository
                .getActiveCoursesWithAssignments()
                .collectAsState(emptyList())
                .value ?: emptyList()
            GlanceTheme(
                colors = GlanceTheme.colors
            ) {
                Content(
                    coursesWithAssignments,
                    onRefresh = { updateAllWidgets(context) },
                    modifier = GlanceModifier.clickable(
                        onClick = androidx.glance.action.actionStartActivity<MainActivity>()
                    )
                )
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            GlanceTheme(
                colors = GlanceTheme.colors
            ) {
                Content(
                    coursesWithAssignments = listOf(
                        CourseWithAssignments(
                            sampleCourse,
                            listOf(sampleAssignment)
                        )
                    )
                )
            }
        }
    }

    @Composable
    fun Content(
        coursesWithAssignments: List<CourseWithAssignments>,
        modifier: GlanceModifier = GlanceModifier,
        onRefresh: () -> Unit = {}
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground),
        ) {
            TitleBar(
                startIcon = ImageProvider(R.drawable.rounded_checklist),
                title = LocalContext.current.getString(R.string.assignments),
                iconColor = GlanceTheme.colors.primary,
                actions = {
                    IconImage(
                        modifier = GlanceModifier.size(48.dp),
                        provider = ImageProvider(R.drawable.rounded_refresh),
                        iconColor = GlanceTheme.colors.primary,
                        onClick = onRefresh
                    )
                }
            )
            LazyColumn {
                items(coursesWithAssignments.filter {
                    it.assignments.any { assignment ->
                        assignment.dueTime > Clock.System.now()
                    }
                }) {
                    CourseSection(
                        courseWithAssignments = it,
                        modifier = GlanceModifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

    }

    @Composable
    fun CourseSection(
        courseWithAssignments: CourseWithAssignments,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Column(
            modifier = modifier
        ) {
            Text(
                courseWithAssignments.course.chineseName,
                maxLines = 1,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground
                )
            )
            courseWithAssignments.assignments.filter {
                it.dueTime > Clock.System.now()
            }.forEach {
                val intent = openUrlIntent(it.htmlUrl)
                AssignmentListItem(
                    assignment = it,
                    modifier = GlanceModifier.padding(vertical = 2.dp),
                    onClickAction = actionStartActivity(intent)
                )
            }
        }
    }

    @Composable
    fun AssignmentListItem(
        assignment: Assignment,
        modifier: GlanceModifier = GlanceModifier,
        onClickAction: Action? = null
    ) {
        val textStyle = TextStyle(
            color = GlanceTheme.colors.onPrimaryContainer,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )

        Box(
            modifier = modifier
        ) {
            var modifier = GlanceModifier
                .cornerRadius(16.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .padding(horizontal = 12.dp)
                .padding(vertical = 2.dp)
                .fillMaxWidth()
            if (onClickAction != null)
                modifier = modifier.clickable(onClickAction)
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.name,
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                    style = textStyle.copy(
                        textDecoration = if (assignment.submitted) TextDecoration.LineThrough else null
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = assignment.remainingTimeOneUnit(LocalContext.current),
                    maxLines = 1,
                    style = textStyle
                )
            }
        }
    }

    inner class AssignmentsWidgetRefreshWorker(
        private val context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {
        override suspend fun doWork(): Result {
            AssignmentsWidget().updateAll(context)
            return Result.success()
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val constraints = Constraints.Builder()
                .build()
            val workRequest = OneTimeWorkRequestBuilder<AssignmentsWidgetRefreshWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(
                "Refresh assignments widget",
                ExistingWorkPolicy.KEEP,
                workRequest
            ).enqueue()
        }
    }
}