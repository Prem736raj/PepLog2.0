package com.appvexis.peptidetracker.feature.inventory.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.feature.inventory.model.toUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/** Checks inventory even when the user has not opened the Inventory screen. */
@HiltWorker
class InventoryNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val inventoryRepository: InventoryRepository,
    private val peptideRepository: PeptideRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        val peptides = peptideRepository.getAllPeptides().first().associateBy { it.id }
        val vials = inventoryRepository.getAllInventoryItems().first().map { item ->
            val peptide = peptides[item.peptideId]
            item.toUiModel(
                peptideName = peptide?.name ?: item.peptideId,
                peptideCategory = peptide?.category ?: "Custom peptide"
            )
        }
        InventoryNotificationHelper.checkAndNotifyInventoryAlerts(applicationContext, vials)
        Result.success()
    } catch (error: Exception) {
        Timber.e(error, "Inventory notification worker failed")
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "inventory_alert_check"
    }
}
