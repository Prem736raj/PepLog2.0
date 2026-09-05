package com.appvexis.peptidetracker.feature.inventory.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules a low-cost daily inventory check independent of screen lifecycle. */
@Singleton
class InventoryNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleDailyCheck() {
        val request = PeriodicWorkRequestBuilder<InventoryNotificationWorker>(
            24, TimeUnit.HOURS,
            2, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            InventoryNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        workManager.enqueueUniqueWork(
            "${InventoryNotificationWorker.WORK_NAME}_bootstrap",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<InventoryNotificationWorker>().build()
        )
        Timber.d("Inventory alert worker scheduled (daily)")
    }
}
