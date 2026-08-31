package com.appvexis.peptidetracker.feature.inventory.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.appvexis.peptidetracker.feature.inventory.model.ExpirationStatus
import com.appvexis.peptidetracker.feature.inventory.model.VialUiModel
import timber.log.Timber

/**
 * Local notification helper for inventory low-stock and 28-day expiration alerts.
 */
object InventoryNotificationHelper {

    private const val CHANNEL_ID = "peplog_inventory_alerts"
    private const val CHANNEL_NAME = "Inventory & Expiration Alerts"
    private const val CHANNEL_DESC = "Notifications for expiring peptide vials and low stock alerts"
    private const val NOTIFICATION_ID_EXPIRATION = 1001
    private const val NOTIFICATION_ID_LOW_STOCK = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkAndNotifyInventoryAlerts(context: Context, vials: List<VialUiModel>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                "android.permission.POST_NOTIFICATIONS"
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createNotificationChannel(context)

        val criticalVials = vials.filter { it.expirationStatus == ExpirationStatus.CRITICAL || it.expirationStatus == ExpirationStatus.EXPIRING_SOON }
        val expiredVials = vials.filter { it.expirationStatus == ExpirationStatus.EXPIRED }
        val lowStockVials = vials.filter { it.isLowVolume }

        // Alert for expiring vials
        if (criticalVials.isNotEmpty() || expiredVials.isNotEmpty()) {
            val title = when {
                expiredVials.isNotEmpty() -> "⚠️ Peptide Expiration Alert"
                criticalVials.any { it.expirationStatus == ExpirationStatus.CRITICAL } -> "⏳ Peptide Expiring in ≤3 Days"
                else -> "ℹ️ Peptide Shelf Life Notice"
            }

            val summaryText = buildString {
                if (expiredVials.isNotEmpty()) {
                    append("${expiredVials.size} vial(s) expired. ")
                }
                if (criticalVials.isNotEmpty()) {
                    append("${criticalVials.size} vial(s) need an expiration review.")
                }
            }

            sendNotification(
                context = context,
                notificationId = NOTIFICATION_ID_EXPIRATION,
                title = title,
                message = summaryText
            )
        }

        // Alert for low volume stock
        if (lowStockVials.isNotEmpty()) {
            sendNotification(
                context = context,
                notificationId = NOTIFICATION_ID_LOW_STOCK,
                title = "📦 Low Vial Volume Alert",
                message = "${lowStockVials.size} vial(s) have low remaining volume. Open PepLog for details."
            )
        }
    }

    private fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = launchIntent?.let {
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .apply {
                    if (pendingIntent != null) setContentIntent(pendingIntent)
                }

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Timber.w(e, "Notification permission not granted for inventory alerts")
        } catch (e: Exception) {
            Timber.e(e, "Failed to post inventory notification")
        }
    }
}
