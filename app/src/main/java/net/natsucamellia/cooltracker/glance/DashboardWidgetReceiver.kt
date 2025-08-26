package net.natsucamellia.cooltracker.glance

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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
import net.natsucamellia.cooltracker.model.sampleAssignment
import kotlin.time.Clock

class DashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DashboardWidget()
}

class DashboardWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("DashboardWidget", "provideGlance")
        val application: CoolApplication = context as CoolApplication
        val coolRepository = application.container.coolRepository

        provideContent {
            val assignments = coolRepository
                .getActiveCoursesWithAssignments()
                .collectAsState(emptyList())
                .value
                ?.flatMap { it.assignments } ?: emptyList()
            GlanceTheme(
                GlanceTheme.colors
            ) {
                Dashboard(
                    assignments = assignments,
                    onRefresh = {
                        updateAllWidgets(context)
                    },
                    modifier = GlanceModifier.clickable(
                        onClick = actionStartActivity<MainActivity>()
                    )
                )
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        Log.d("DashboardWidget", "providePreview")
        provideContent {
            GlanceTheme(
                GlanceTheme.colors
            ) {
                Dashboard(
                    assignments = listOf(sampleAssignment),
                )
            }
        }
    }

    @Composable
    fun Dashboard(
        assignments: List<Assignment>,
        modifier: GlanceModifier = GlanceModifier,
        onRefresh: () -> Unit = {}
    ) {
        val now = Clock.System.now()
        val ongoingAssignment = assignments.filter { it.dueTime > now }
        val pending = ongoingAssignment.count { !it.submitted }
        val ongoing = ongoingAssignment.size
        val closed = assignments.size - ongoing
        val submitted = assignments.count { it.submitted }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 12.dp)
                .background(GlanceTheme.colors.widgetBackground),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleBar(
                startIcon = ImageProvider(R.drawable.rounded_checklist),
                title = LocalContext.current.getString(R.string.dashboard),
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
            ListItem(
                startIcon = ImageProvider(R.drawable.rounded_hourglass_top),
                title = LocalContext.current.getString(R.string.pending),
                text = pending.toString(),
                modifier = GlanceModifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            )
            Spacer(GlanceModifier.height(8.dp))
            ListItem(
                startIcon = ImageProvider(R.drawable.rounded_play_circle),
                title = LocalContext.current.getString(R.string.ongoing),
                text = ongoing.toString(),
                modifier = GlanceModifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            )
            Spacer(GlanceModifier.height(8.dp))
            ListItem(
                startIcon = ImageProvider(R.drawable.rounded_stop_circle),
                title = LocalContext.current.getString(R.string.closed),
                text = closed.toString(),
                modifier = GlanceModifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            )
            Spacer(GlanceModifier.height(8.dp))
            ListItem(
                startIcon = ImageProvider(R.drawable.rounded_check),
                title = LocalContext.current.getString(R.string.submitted),
                text = submitted.toString(),
                modifier = GlanceModifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
            )
        }
    }

    class RefreshWorker(
        private val context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {
        override suspend fun doWork(): Result {
            DashboardWidget().updateAll(context)
            return Result.success()
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val constraints = Constraints.Builder()
                .build()
            val workRequest = OneTimeWorkRequestBuilder<RefreshWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).beginUniqueWork(
                "Refresh dashboard widget",
                ExistingWorkPolicy.KEEP,
                workRequest
            ).enqueue()
        }
    }
}

@Composable
fun ListItem(
    startIcon: ImageProvider,
    title: String,
    text: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val textStyle = TextStyle(
        color = GlanceTheme.colors.outline,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconImage(
            provider = startIcon,
            iconColor = GlanceTheme.colors.primary,
            iconSize = 20.dp
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = title,
            modifier = GlanceModifier.defaultWeight(),
            style = textStyle
        )
        Text(
            text = text,
            style = textStyle
        )
    }
}

@Composable
fun IconImage(
    provider: ImageProvider,
    contentDescription: String? = null,
    modifier: GlanceModifier = GlanceModifier,
    iconColor: ColorProvider? = null,
    iconSize: Dp = 24.dp,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        var modifier: GlanceModifier = GlanceModifier
        if (onClick != null) {
            modifier = modifier.clickable(onClick)
        }
        Image(
            modifier = modifier
                .size(iconSize)
                .cornerRadius(iconSize / 2),
            provider = provider,
            contentDescription = contentDescription,
            colorFilter = iconColor?.let { ColorFilter.tint(it) }
        )
    }
}
