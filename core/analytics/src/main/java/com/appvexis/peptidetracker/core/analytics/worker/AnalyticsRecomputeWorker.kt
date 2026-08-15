package com.appvexis.peptidetracker.core.analytics.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appvexis.peptidetracker.core.analytics.AnalyticsEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Background worker that performs nightly recomputation of all precomputed analytics summaries.
 */
@HiltWorker
class AnalyticsRecomputeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val analyticsEngine: AnalyticsEngine
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.i("Starting scheduled AnalyticsRecomputeWorker")
        return try {
            analyticsEngine.recomputeAll()
            Timber.i("AnalyticsRecomputeWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "AnalyticsRecomputeWorker failed")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "analytics_nightly_recompute"
    }
}
