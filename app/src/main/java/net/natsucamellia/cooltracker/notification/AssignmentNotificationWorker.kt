package net.natsucamellia.cooltracker.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.natsucamellia.cooltracker.MainActivity
import net.natsucamellia.cooltracker.R

class AssignmentNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // Check if the notification permission is granted
        // We check here because we want to schedule notifications even the notification permission
        // is not granted. So the user can enable it later and the notifications will still show up.
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return Result.failure()
        }

        val title = inputData.getString("title")
        val text = inputData.getString("text")
        val id = inputData.getInt("id", 0)

        // Launch the app when the notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(
            applicationContext,
            CoolNotificationManager.ASSIGNMENT_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.rounded_checklist)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Automatically dismiss the notification when it's clicked

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
        return Result.success()
    }
}