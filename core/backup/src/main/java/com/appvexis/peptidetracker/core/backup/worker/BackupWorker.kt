package com.appvexis.peptidetracker.core.backup.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appvexis.peptidetracker.core.backup.BackupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * WorkManager worker for scheduled automatic backups.
 * Runs periodically (default: every 24 hours) to back up data to Google Drive
 * when auto-backup is enabled and a Google account is linked.
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupManager: BackupManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("BackupWorker: starting scheduled backup")

        val accountEmail = backupManager.getGoogleAccountEmailSync()
        if (accountEmail.isNullOrBlank()) {
            Timber.w("BackupWorker: no Google account linked, skipping backup")
            return Result.success()
        }

        return try {
            val result = backupManager.backupToDrive(accountEmail)
            if (result.isSuccess) {
                Timber.d("BackupWorker: scheduled backup succeeded")
                Result.success()
            } else {
                Timber.e("BackupWorker: scheduled backup failed — ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "BackupWorker: unexpected error during scheduled backup")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "peplog_auto_backup"
    }
}
