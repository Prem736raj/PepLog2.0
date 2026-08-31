package com.appvexis.peptidetracker.core.backup

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appvexis.peptidetracker.core.backup.drive.DriveBackupService
import com.appvexis.peptidetracker.core.backup.export.CsvExporter
import com.appvexis.peptidetracker.core.backup.export.DatabaseExporter
import com.appvexis.peptidetracker.core.backup.export.DatabaseImporter
import com.appvexis.peptidetracker.core.backup.model.BackupData
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import com.appvexis.peptidetracker.core.backup.model.DriveBackupFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.backupPreferences by preferencesDataStore(name = "backup_preferences")

/**
 * Orchestrates backup, restore, and export operations.
 * Coordinates between [DatabaseExporter], [CsvExporter], and [DriveBackupService].
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseExporter: DatabaseExporter,
    private val databaseImporter: DatabaseImporter,
    private val csvExporter: CsvExporter,
    private val driveBackupService: DriveBackupService
) {
    // Preference keys
    private val lastBackupTimeKey = longPreferencesKey(BackupConfig.PREF_LAST_BACKUP_TIME)
    private val lastBackupSizeKey = longPreferencesKey(BackupConfig.PREF_LAST_BACKUP_SIZE)
    private val autoBackupEnabledKey = booleanPreferencesKey(BackupConfig.PREF_AUTO_BACKUP_ENABLED)
    private val googleAccountEmailKey = stringPreferencesKey(BackupConfig.PREF_GOOGLE_ACCOUNT_EMAIL)

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    // ---- Preference Flows ---- //

    val lastBackupTime: Flow<Long?> = context.backupPreferences.data
        .map { prefs -> prefs[lastBackupTimeKey] }

    val lastBackupSize: Flow<Long?> = context.backupPreferences.data
        .map { prefs -> prefs[lastBackupSizeKey] }

    val isAutoBackupEnabled: Flow<Boolean> = context.backupPreferences.data
        .map { prefs -> prefs[autoBackupEnabledKey] ?: false }

    val googleAccountEmail: Flow<String?> = context.backupPreferences.data
        .map { prefs -> prefs[googleAccountEmailKey] }

    // ---- Backup Operations ---- //

    /**
     * Performs a full backup to Google Drive.
     * 1. Exports database to BackupData
     * 2. Serializes to JSON
     * 3. Uploads to Drive appDataFolder
     * 4. Updates last backup timestamp and size
     */
    suspend fun backupToDrive(accountEmail: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.1f, "Exporting database...")
            val backupData = databaseExporter.exportToBackupData()

            _backupStatus.value = BackupStatus.InProgress(0.4f, "Serializing data...")
            val json = databaseExporter.toJson(backupData)
            val sizeBytes = json.toByteArray(Charsets.UTF_8).size.toLong()

            _backupStatus.value = BackupStatus.InProgress(0.6f, "Uploading to Google Drive...")
            val fileId = driveBackupService.uploadBackup(accountEmail, json)

            // Save backup metadata
            val timestamp = System.currentTimeMillis()
            context.backupPreferences.edit { prefs ->
                prefs[lastBackupTimeKey] = timestamp
                prefs[lastBackupSizeKey] = sizeBytes
                prefs[googleAccountEmailKey] = accountEmail
            }

            _backupStatus.value = BackupStatus.Success(timestamp, sizeBytes)
            Timber.d("BackupManager: backup complete — fileId=$fileId, size=$sizeBytes bytes")
            Result.success(fileId)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: backup failed")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Backup failed")
            Result.failure(e)
        }
    }

    /**
     * Restores data from a Google Drive backup file.
     */
    suspend fun restoreFromDrive(
        accountEmail: String,
        fileId: String
    ): Result<BackupData> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Downloading from Google Drive...")
            val json = driveBackupService.downloadBackup(accountEmail, fileId)

            _backupStatus.value = BackupStatus.InProgress(0.6f, "Parsing backup data...")
            val backupData = databaseExporter.fromJson(json)

            _backupStatus.value = BackupStatus.InProgress(0.8f, "Restoring database...")
            databaseImporter.restore(backupData)

            _backupStatus.value = BackupStatus.Success(
                timestamp = backupData.createdAt,
                sizeBytes = json.toByteArray(Charsets.UTF_8).size.toLong()
            )

            Timber.d("BackupManager: restore complete — ${backupData.protocols.size} protocols, ${backupData.doseLogs.size} dose logs")
            Result.success(backupData)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: restore failed")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Restore failed")
            Result.failure(e)
        }
    }

    /**
     * Lists available backup files on Google Drive.
     */
    suspend fun listDriveBackups(accountEmail: String): Result<List<DriveBackupFile>> {
        return try {
            val backups = driveBackupService.listBackups(accountEmail)
            Result.success(backups)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: list backups failed")
            Result.failure(e)
        }
    }

    /**
     * Deletes a specific backup from Google Drive.
     */
    suspend fun deleteBackup(accountEmail: String, fileId: String): Result<Unit> {
        return try {
            driveBackupService.deleteBackup(accountEmail, fileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: delete failed")
            Result.failure(e)
        }
    }

    // ---- Local Export Operations ---- //

    /**
     * Exports all data as CSV files to the specified directory.
     * Returns a list of generated files.
     */
    suspend fun exportToCsv(outputDir: File): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Exporting database...")
            val data = databaseExporter.exportToBackupData()

            _backupStatus.value = BackupStatus.InProgress(0.6f, "Generating CSV files...")
            val files = csvExporter.exportAll(data, outputDir)

            _backupStatus.value = BackupStatus.Success(
                timestamp = System.currentTimeMillis(),
                sizeBytes = files.sumOf { it.length() }
            )
            Result.success(files)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: CSV export failed")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Export failed")
            Result.failure(e)
        }
    }

    /**
     * Exports all data as a single JSON file.
     */
    suspend fun exportToJson(outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Exporting database...")
            val data = databaseExporter.exportToBackupData()

            _backupStatus.value = BackupStatus.InProgress(0.6f, "Writing JSON...")
            val json = databaseExporter.toJson(data)
            outputFile.parentFile?.mkdirs()
            outputFile.writeText(json, Charsets.UTF_8)

            _backupStatus.value = BackupStatus.Success(
                timestamp = System.currentTimeMillis(),
                sizeBytes = outputFile.length()
            )
            Result.success(outputFile)
        } catch (e: Exception) {
            Timber.e(e, "BackupManager: JSON export failed")
            _backupStatus.value = BackupStatus.Error(e.message ?: "Export failed")
            Result.failure(e)
        }
    }

    // ---- Auto-Backup Preferences ---- //

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.backupPreferences.edit { prefs ->
            prefs[autoBackupEnabledKey] = enabled
        }
    }

    suspend fun setGoogleAccountEmail(email: String?) {
        context.backupPreferences.edit { prefs ->
            if (email != null) {
                prefs[googleAccountEmailKey] = email
            } else {
                prefs.remove(googleAccountEmailKey)
                // Never leave a scheduled cloud export enabled after disconnecting.
                prefs[autoBackupEnabledKey] = false
            }
        }
    }

    suspend fun getGoogleAccountEmailSync(): String? {
        return context.backupPreferences.data.first()[googleAccountEmailKey]
    }

    suspend fun isAutoBackupEnabledSync(): Boolean {
        return context.backupPreferences.data.first()[autoBackupEnabledKey] ?: false
    }

    fun resetStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}
