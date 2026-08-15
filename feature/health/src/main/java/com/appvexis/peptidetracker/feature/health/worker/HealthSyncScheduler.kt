package com.appvexis.peptidetracker.feature.health.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules periodic Health Connect sync via WorkManager.
 * Runs every 6 hours with battery constraints.
 */
@Singleton
class HealthSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Enqueues a 6-hour periodic work request for Health Connect sync.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<HealthSyncWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES // 30-minute flex window
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HealthSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Timber.d("Health Connect periodic sync worker scheduled (every 6 hours)")
    }
}
