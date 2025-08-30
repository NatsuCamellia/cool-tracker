package net.natsucamellia.cooltracker.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.until
import net.natsucamellia.cooltracker.R
import net.natsucamellia.cooltracker.data.CoolRepository
import net.natsucamellia.cooltracker.model.Assignment
import net.natsucamellia.cooltracker.model.Course
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

/**
 * This class listens to the active courses with assignments from the repository and schedules
 * notifications for each assignment.
 */
class CoolNotificationManager(
    private val context: Context,
    private val coolRepository: CoolRepository
) {
    init {
        createNotificationChannelGroup()
        createNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            // Observe the active courses with assignments from the repository
            coolRepository.getActiveCoursesWithAssignments().collect {
                it?.forEach { courseWithAssignments ->
                    courseWithAssignments.assignments.forEach { assignment ->
                        if (assignment.dueTime > Clock.System.now()) {
                            scheduleAssignmentNotification(courseWithAssignments.course, assignment)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleAssignmentNotification(course: Course, assignment: Assignment) {
        val courseName = course.name
        val assignmentName = assignment.name
        val assignmentId = assignment.id
        val workManager = WorkManager.getInstance(context)

        val now = Clock.System.now()
        val reminderTime3d = assignment.dueTime.minus(72, DateTimeUnit.HOUR)
        val reminderTime1d = assignment.dueTime.minus(24, DateTimeUnit.HOUR)

        if (reminderTime3d > now) {
            val delayMillis3d = now.until(reminderTime3d, DateTimeUnit.MILLISECOND)
            val notification3d =
                OneTimeWorkRequest.Builder(AssignmentNotificationWorker::class.java)
                    .setInitialDelay(delayMillis3d, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "title" to courseName,
                            "text" to context.getString(R.string.notification_3d, assignmentName),
                            "id" to assignment.id
                        )
                    )
                    .build()
            workManager.enqueueUniqueWork(
                "3d_$assignmentId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                notification3d
            )
        }

        if (reminderTime1d > now) {
            val delayMillis1d = now.until(reminderTime1d, DateTimeUnit.MILLISECOND)
            val notification1d =
                OneTimeWorkRequest.Builder(AssignmentNotificationWorker::class.java)
                    .setInitialDelay(delayMillis1d, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "title" to courseName,
                            "text" to context.getString(R.string.notification_1d, assignmentName),
                            "id" to assignmentId
                        )
                    )
                    .build()
            workManager.enqueueUniqueWork(
                "1d_$assignmentId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                notification1d
            )
        }
    }

    private fun createNotificationChannelGroup() {
        val name = context.getString(R.string.channel_group_assignments)
        val channelGroup = NotificationChannelGroup(ASSIGNMENT_CHANNEL_GROUP_ID, name)
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelGroup(channelGroup)
    }

    private fun createNotificationChannel() {
        val name = context.getString(R.string.channel_upcoming_assignments)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel =
            NotificationChannel(ASSIGNMENT_CHANNEL_ID, name, importance).apply {
                group = ASSIGNMENT_CHANNEL_GROUP_ID
            }
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object Companion {
        const val ASSIGNMENT_CHANNEL_ID = "assignments_channel"
        const val ASSIGNMENT_CHANNEL_GROUP_ID = "assignments_group"
    }
}