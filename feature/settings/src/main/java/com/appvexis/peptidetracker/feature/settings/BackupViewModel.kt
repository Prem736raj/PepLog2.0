package com.appvexis.peptidetracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun resetStatus() = backupManager.resetStatus()

    fun clearShareRequest() {
        _shareRequest.value = null
    }
}
