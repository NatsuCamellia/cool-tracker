package net.natsucamellia.cooltracker.util

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.core.net.toUri

fun openUrlIntent(url: String): Intent {
    return Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(FLAG_ACTIVITY_NEW_TASK)
    }
}

fun openUrl(context: Context, url: String) {
    context.startActivity(openUrlIntent(url))
}