package com.appvexis.peptidetracker.feature.health.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appvexis.peptidetracker.feature.health.data.HealthConnectSyncEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Background worker that periodically syncs health data from Health Connect into Room.
 * Runs every 6 hours with battery-not-low constraint.
 */
@HiltWorker
class HealthSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEngine: HealthConnectSyncEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.i("Starting scheduled HealthSyncWorker")
        return try {
            val count = syncEngine.syncAll()
            Timber.i("HealthSyncWorker completed: $count records synced")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "HealthSyncWorker failed")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "health_connect_periodic_sync"
    }
}
