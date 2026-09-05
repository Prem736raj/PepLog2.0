package com.appvexis.peptidetracker.feature.log.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/** WorkManager-backed dose reminder; it deliberately remains inexact and battery-light. */
@HiltWorker
class DoseReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val logRepository: LogRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        val now = System.currentTimeMillis()
        val doses = logRepository.getDoseLogs(now, now + REMINDER_WINDOW_MS)
            .first()
            .filter { it.status == DoseStatus.PENDING && it.scheduledTime >= now }
        DoseReminderHelper.checkAndNotify(applicationContext, doses)
        Result.success()
    } catch (error: Exception) {
        Timber.e(error, "Dose reminder worker failed")
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "dose_reminder_periodic_check"
        const val BOOTSTRAP_WORK_NAME = "dose_reminder_bootstrap_check"
        private const val REMINDER_WINDOW_MS = 30L * 60L * 1000L
    }
}
