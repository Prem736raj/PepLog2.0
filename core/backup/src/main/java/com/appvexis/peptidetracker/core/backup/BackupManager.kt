package com.appvexis.peptidetracker.core.backup

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appvexis.peptidetracker.core.backup.export.CsvExporter
import com.appvexis.peptidetracker.core.backup.export.DatabaseExporter
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.backupPreferences by preferencesDataStore(name = "backup_preferences")

/**
 * Creates portable, user-triggered local exports. Data never leaves the device until the
 * user chooses an app in Android's system share sheet.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseExporter: DatabaseExporter,
    private val csvExporter: CsvExporter,
) {
    private val lastExportTimeKey = longPreferencesKey("last_export_time_ms")
    private val lastExportSizeKey = longPreferencesKey("last_export_size_bytes")

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    val lastExportTime: Flow<Long?> = context.backupPreferences.data.map { it[lastExportTimeKey] }
    val lastExportSize: Flow<Long?> = context.backupPreferences.data.map { it[lastExportSizeKey] }

    /** Exports separate CSV files for records that are useful in a spreadsheet. */
    suspend fun exportToCsv(outputDir: File): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Preparing your records")
            val data = databaseExporter.exportToBackupData()
            _backupStatus.value = BackupStatus.InProgress(0.65f, "Writing CSV files")
            val files = csvExporter.exportAll(data, outputDir)
            completeExport(files.sumOf(File::length))
            Result.success(files)
        } catch (error: Exception) {
            failExport(error)
        }
    }

    /** Exports a complete, portable JSON snapshot. */
    suspend fun exportToJson(outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Preparing your records")
            val data = databaseExporter.exportToBackupData()
            _backupStatus.value = BackupStatus.InProgress(0.65f, "Writing JSON file")
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(databaseExporter.toJson(data), Charsets.UTF_8)
            completeExport(outputFile.length())
            Result.success(outputFile)
        } catch (error: Exception) {
            failExport(error)
        }
    }

    private suspend fun completeExport(sizeBytes: Long) {
        val timestamp = System.currentTimeMillis()
        context.backupPreferences.edit { preferences ->
            preferences[lastExportTimeKey] = timestamp
            preferences[lastExportSizeKey] = sizeBytes
        }
        _backupStatus.value = BackupStatus.Success(timestamp, sizeBytes)
    }

    private fun <T> failExport(error: Exception): Result<T> {
        Timber.e(error, "BackupManager: export failed")
        _backupStatus.value = BackupStatus.Error(error.message ?: "The export could not be created")
        return Result.failure(error)
    }

    fun resetStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}
