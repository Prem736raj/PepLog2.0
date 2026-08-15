package com.appvexis.peptidetracker.core.analytics.worker

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
 * Helper to schedule nightly periodic analytics recomputation.
 */
@Singleton
class AnalyticsWorkerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Enqueues a 24-hour periodic work request for analytics recomputation.
     */
    fun scheduleNightlyRecompute() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<AnalyticsRecomputeWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS // 1-hour flex window
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AnalyticsRecomputeWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Timber.d("Nightly analytics recompute worker scheduled")
    }
}
