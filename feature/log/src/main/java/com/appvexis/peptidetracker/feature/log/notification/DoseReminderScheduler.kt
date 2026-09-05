package com.appvexis.peptidetracker.feature.log.notification

import android.content.Context
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

/** Keeps dose reminders alive without exact-alarm permission or a screen collector. */
@Singleton
class DoseReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule() {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            DoseReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<DoseReminderWorker>(15, TimeUnit.MINUTES).build()
        )
        workManager.enqueueUniqueWork(
            DoseReminderWorker.BOOTSTRAP_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<DoseReminderWorker>().build()
        )
        Timber.d("Dose reminder worker scheduled (15-minute checks)")
    }
}
