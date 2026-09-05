package com.appvexis.peptidetracker.feature.log.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.appvexis.peptidetracker.core.model.DoseLog
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Posts one quiet, deduplicated reminder for the next scheduled dose window. */
object DoseReminderHelper {
    private const val CHANNEL_ID = "peplog_dose_reminders"
    private const val CHANNEL_NAME = "Dose reminders"
    private const val CHANNEL_DESCRIPTION = "Optional reminders for scheduled doses"
    private const val NOTIFICATION_ID = 2001
    private const val PREFS_NAME = "dose_reminder_state"
    private const val LAST_SIGNATURE_KEY = "last_signature"

    fun checkAndNotify(context: Context, doses: List<DoseLog>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createNotificationChannel(context)
        val manager = NotificationManagerCompat.from(context)
        if (doses.isEmpty()) {
            manager.cancel(NOTIFICATION_ID)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(LAST_SIGNATURE_KEY)
                .apply()
            return
        }

        val ordered = doses.sortedBy { it.scheduledTime }
        val signature = ordered.joinToString(separator = "|") { "${it.id}:${it.scheduledTime}" }
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.getString(LAST_SIGNATURE_KEY, null) == signature) return

        val firstTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
            .format(Date(ordered.first().scheduledTime))
        val message = if (ordered.size == 1) {
            "1 scheduled dose is due around $firstTime."
        } else {
            "${ordered.size} scheduled doses are due soon. The first is around $firstTime."
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Scheduled dose reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()

        manager.notify(NOTIFICATION_ID, notification)
        preferences.edit().putString(LAST_SIGNATURE_KEY, signature).apply()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESCRIPTION }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
