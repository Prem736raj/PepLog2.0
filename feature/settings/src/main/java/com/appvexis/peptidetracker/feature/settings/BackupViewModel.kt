package com.appvexis.peptidetracker.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.backup.BackupConfig
import com.appvexis.peptidetracker.core.backup.BackupManager
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/** Controls the local, user-initiated export flow. */
@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager,
) : ViewModel() {

    data class ShareRequest(val files: List<File>, val mimeType: String)

    data class ExportPreferences(
        val lastExportTime: Long? = null,
        val lastExportSize: Long? = null,
    )

    val backupStatus: StateFlow<BackupStatus> = backupManager.backupStatus
    val preferences: StateFlow<ExportPreferences> = combine(
        backupManager.lastExportTime,
        backupManager.lastExportSize,
    ) { lastTime, lastSize ->
        ExportPreferences(lastExportTime = lastTime, lastExportSize = lastSize)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExportPreferences(),
    )

    private val _shareRequest = MutableStateFlow<ShareRequest?>(null)
    val shareRequest: StateFlow<ShareRequest?> = _shareRequest.asStateFlow()

    fun exportCsv() {
        viewModelScope.launch {
            val exportDir = File(context.cacheDir, "exports/csv").apply { mkdirs() }
            backupManager.exportToCsv(exportDir).onSuccess { files ->
                _shareRequest.value = ShareRequest(files = files, mimeType = "text/csv")
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            val exportFile = File(context.cacheDir, "exports/peplog_full_export.json")
            backupManager.exportToJson(exportFile).onSuccess { file ->
                _shareRequest.value = ShareRequest(files = listOf(file), mimeType = "application/json")
            }
        }
    }

    fun exportArchive() {
        viewModelScope.launch {
            val exportFile = File(context.cacheDir, "exports/${UUID.randomUUID()}_${BackupConfig.EXPORT_FULL_ARCHIVE}")
            backupManager.exportToArchive(exportFile).onSuccess { file ->
                _shareRequest.value = ShareRequest(files = listOf(file), mimeType = BackupConfig.MIME_TYPE_ARCHIVE)
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val exportFile = File(context.cacheDir, "exports/${UUID.randomUUID()}_${BackupConfig.EXPORT_CLINICIAN_REPORT}")
            backupManager.exportToPdf(exportFile).onSuccess { file ->
                _shareRequest.value = ShareRequest(files = listOf(file), mimeType = BackupConfig.MIME_TYPE_PDF)
            }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch {
            val importFile = File(context.cacheDir, "imports/${UUID.randomUUID()}_peplog_backup")
            runCatching {
                importFile.parentFile?.mkdirs()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(importFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= MAX_IMPORT_BYTES) { "Backup file is too large" }
                            output.write(buffer, 0, count)
                        }
                    }
                } ?: error("The selected backup file could not be opened")
                backupManager.importFromFile(importFile)
            }.onFailure(backupManager::reportError)
            importFile.delete()
        }
    }

    fun resetStatus() = backupManager.resetStatus()

    fun clearShareRequest() {
        _shareRequest.value = null
    }

    private companion object {
        const val MAX_IMPORT_BYTES = 200L * 1024L * 1024L
    }
}
