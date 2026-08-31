package com.appvexis.peptidetracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.backup.BackupManager
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import com.appvexis.peptidetracker.core.backup.model.DriveBackupFile
import com.appvexis.peptidetracker.core.billing.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for the Backup & Export settings screen.
 * Manages Google Drive backup/restore, CSV/JSON export, and auto-backup scheduling.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager,
    private val subscriptionManager: SubscriptionManager
) : ViewModel() {

    data class ShareRequest(
        val files: List<File>,
        val mimeType: String
    )

    val backupStatus: StateFlow<BackupStatus> = backupManager.backupStatus

    private val _driveBackups = MutableStateFlow<List<DriveBackupFile>>(emptyList())
    val driveBackups: StateFlow<List<DriveBackupFile>> = _driveBackups.asStateFlow()

    private val _isLoadingBackups = MutableStateFlow(false)
    val isLoadingBackups: StateFlow<Boolean> = _isLoadingBackups.asStateFlow()

    private val _showRestoreConfirmation = MutableStateFlow<DriveBackupFile?>(null)
    val showRestoreConfirmation: StateFlow<DriveBackupFile?> = _showRestoreConfirmation.asStateFlow()

    private val _shareRequest = MutableStateFlow<ShareRequest?>(null)
    val shareRequest: StateFlow<ShareRequest?> = _shareRequest.asStateFlow()

    data class BackupPreferences(
        val lastBackupTime: Long? = null,
        val lastBackupSize: Long? = null,
        val isAutoBackupEnabled: Boolean = false,
        val googleAccountEmail: String? = null,
        val isPremium: Boolean = false
    )

    val preferences: StateFlow<BackupPreferences> = combine(
        backupManager.lastBackupTime,
        backupManager.lastBackupSize,
        backupManager.isAutoBackupEnabled,
        backupManager.googleAccountEmail,
        subscriptionManager.isPremium
    ) { lastTime, lastSize, autoEnabled, email, isPremium ->
        BackupPreferences(
            lastBackupTime = lastTime,
            lastBackupSize = lastSize,
            isAutoBackupEnabled = autoEnabled,
            googleAccountEmail = email,
            isPremium = isPremium
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BackupPreferences()
    )

    /**
     * Triggers a manual backup to Google Drive.
     */
    fun backupNow() {
        if (!subscriptionManager.isPremium.value) {
            Timber.w("BackupViewModel: cloud backup requires Premium")
            return
        }
        val email = preferences.value.googleAccountEmail
        if (email.isNullOrBlank()) {
            Timber.w("BackupViewModel: no Google account linked")
            return
        }
        viewModelScope.launch {
            backupManager.backupToDrive(email)
        }
    }

    /**
     * Loads the list of available backups from Google Drive.
     */
    fun loadDriveBackups() {
        if (!subscriptionManager.isPremium.value) return
        val email = preferences.value.googleAccountEmail ?: return
        viewModelScope.launch {
            _isLoadingBackups.value = true
            val result = backupManager.listDriveBackups(email)
            result.onSuccess { backups ->
                _driveBackups.value = backups
            }
            _isLoadingBackups.value = false
        }
    }

    /**
     * Shows the restore confirmation dialog for a specific backup.
     */
    fun requestRestore(backup: DriveBackupFile) {
        _showRestoreConfirmation.value = backup
    }

    /**
     * Dismisses the restore confirmation dialog.
     */
    fun dismissRestoreConfirmation() {
        _showRestoreConfirmation.value = null
    }

    /**
     * Restores data from the selected Google Drive backup.
     */
    fun confirmRestore(backup: DriveBackupFile) {
        _showRestoreConfirmation.value = null
        if (!subscriptionManager.isPremium.value) return
        val email = preferences.value.googleAccountEmail ?: return
        viewModelScope.launch {
            val result = backupManager.restoreFromDrive(email, backup.id)
            result.onSuccess { data ->
                Timber.d("BackupViewModel: restore downloaded — ${data.protocols.size} protocols")
            }
        }
    }

    /**
     * Exports all data as CSV files and shares via system share sheet.
     */
    fun exportCsv() {
        viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports")
            val result = backupManager.exportToCsv(exportDir)
            result.onSuccess { files ->
                Timber.d("BackupViewModel: CSV export produced ${files.size} files")
                _shareRequest.value = ShareRequest(files, "text/csv")
            }
        }
    }

    /**
     * Exports all data as a single JSON file.
     */
    fun exportJson() {
        viewModelScope.launch {
            val exportFile = File(context.cacheDir, "exports/peplog_full_export.json")
            exportFile.parentFile?.mkdirs()
            val result = backupManager.exportToJson(exportFile)
            result.onSuccess { file ->
                Timber.d("BackupViewModel: JSON export complete — ${file.length()} bytes")
                _shareRequest.value = ShareRequest(listOf(file), "application/json")
            }
        }
    }

    /**
     * Toggles auto-backup on or off.
     */
    fun setAutoBackup(enabled: Boolean) {
        if (enabled && !subscriptionManager.isPremium.value) return
        viewModelScope.launch {
            backupManager.setAutoBackupEnabled(enabled)
        }
    }

    /**
     * Sets the linked Google account email.
     */
    fun setGoogleAccount(email: String?) {
        viewModelScope.launch {
            backupManager.setGoogleAccountEmail(email)
        }
    }

    /**
     * Resets the backup status to Idle.
     */
    fun resetStatus() {
        backupManager.resetStatus()
    }

    fun clearShareRequest() {
        _shareRequest.value = null
    }
}
