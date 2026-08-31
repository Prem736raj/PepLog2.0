package com.appvexis.peptidetracker.core.backup.drive

import android.content.Context
import com.appvexis.peptidetracker.core.backup.BackupConfig
import com.appvexis.peptidetracker.core.backup.model.DriveBackupFile
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google Drive backup operations using the Drive REST API v3.
 * Operates in the hidden `appDataFolder` scope so backups are invisible
 * to the user's main Drive UI — only accessible via PepLog.
 */
@Singleton
class DriveBackupService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = DateTimeFormatter
        .ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)
        .withZone(ZoneOffset.UTC)

    /**
     * Builds a [Drive] service instance for the given Google account email.
     */
    private fun buildDriveService(accountEmail: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccountName = accountEmail
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("PepLog")
            .build()
    }

    /**
     * Uploads a JSON backup string to Google Drive appDataFolder.
     * Returns the Drive file ID on success.
     */
    suspend fun uploadBackup(
        accountEmail: String,
        jsonData: String
    ): String = withContext(Dispatchers.IO) {
        require(accountEmail.isNotBlank()) { "A Google account is required for backup" }
        Timber.d("DriveBackupService: uploading backup")

        val driveService = buildDriveService(accountEmail)
        val timestamp = dateFormat.format(Instant.now())
        val fileName = "${BackupConfig.BACKUP_FILE_PREFIX}$timestamp${BackupConfig.BACKUP_FILE_EXTENSION}"

        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = fileName
            parents = listOf(BackupConfig.DRIVE_APP_FOLDER_SPACE)
        }

        val mediaContent = ByteArrayContent(
            BackupConfig.MIME_TYPE_JSON,
            jsonData.toByteArray(Charsets.UTF_8)
        )

        val uploadedFile = driveService.files()
            .create(fileMetadata, mediaContent)
            .setFields("id, name, createdTime, size")
            .execute()

        Timber.d("DriveBackupService: uploaded backup")

        // Prune old backups — keep only MAX_BACKUP_HISTORY most recent
        pruneOldBackups(driveService)

        uploadedFile.id
    }

    /**
     * Lists all backup files stored in appDataFolder, sorted by creation time descending.
     */
    suspend fun listBackups(accountEmail: String): List<DriveBackupFile> = withContext(Dispatchers.IO) {
        require(accountEmail.isNotBlank()) { "A Google account is required to list backups" }
        Timber.d("DriveBackupService: listing backups")

        val driveService = buildDriveService(accountEmail)

        val result = driveService.files().list()
            .setSpaces(BackupConfig.DRIVE_APP_FOLDER_SPACE)
            .setQ(backupQuery())
            .setFields("files(id, name, createdTime, size, parents)")
            .setOrderBy("createdTime desc")
            .setPageSize(20)
            .execute()

        val files = result.files?.map { file ->
            DriveBackupFile(
                id = file.id,
                name = file.name,
                createdTime = file.createdTime?.value ?: 0L,
                sizeBytes = file.getSize()?.toLong() ?: 0L
            )
        } ?: emptyList()

        Timber.d("DriveBackupService: found ${files.size} backups")
        files
    }

    /**
     * Downloads backup JSON content by Drive file ID.
     */
    suspend fun downloadBackup(
        accountEmail: String,
        fileId: String
    ): String = withContext(Dispatchers.IO) {
        require(accountEmail.isNotBlank()) { "A Google account is required to download a backup" }

        val driveService = buildDriveService(accountEmail)
        requireOwnedBackupFile(driveService, fileId)
        val outputStream = ByteArrayOutputStream()

        driveService.files()
            .get(fileId)
            .executeMediaAndDownloadTo(outputStream)

        val content = outputStream.toString(Charsets.UTF_8.name())
        Timber.d("DriveBackupService: downloaded backup")
        content
    }

    /**
     * Deletes a backup file from Drive by its file ID.
     */
    suspend fun deleteBackup(
        accountEmail: String,
        fileId: String
    ) = withContext(Dispatchers.IO) {
        require(accountEmail.isNotBlank()) { "A Google account is required to delete a backup" }
        val driveService = buildDriveService(accountEmail)
        requireOwnedBackupFile(driveService, fileId)
        driveService.files().delete(fileId).execute()
    }

    /**
     * Removes older backups beyond the configured maximum history count.
     */
    private fun pruneOldBackups(driveService: Drive) {
        try {
            val result = driveService.files().list()
                .setSpaces(BackupConfig.DRIVE_APP_FOLDER_SPACE)
                .setQ(backupQuery())
                .setFields("files(id, name, createdTime, parents)")
                .setOrderBy("createdTime desc")
                .setPageSize(50)
                .execute()

            val allFiles = result.files ?: return

            if (allFiles.size > BackupConfig.MAX_BACKUP_HISTORY) {
                val toDelete = allFiles.drop(BackupConfig.MAX_BACKUP_HISTORY)
                toDelete.forEach { file ->
                    Timber.d("DriveBackupService: pruning old backup ${file.name}")
                    driveService.files().delete(file.id).execute()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "DriveBackupService: failed to prune old backups")
        }
    }

    private fun backupQuery(): String =
        "'${BackupConfig.DRIVE_APP_FOLDER_SPACE}' in parents and trashed = false " +
            "and name contains '${BackupConfig.BACKUP_FILE_PREFIX}'"

    /** Prevents a caller-controlled ID from reading or deleting another Drive file. */
    private fun requireOwnedBackupFile(
        driveService: Drive,
        fileId: String
    ) {
        require(fileId.isNotBlank() && fileId.length <= 256) { "Invalid backup file ID" }
        val file = driveService.files().get(fileId)
            .setFields("id, name, parents, trashed")
            .execute()
        val isPepLogBackup = file.id == fileId &&
            file.trashed != true &&
            file.parents.orEmpty().contains(BackupConfig.DRIVE_APP_FOLDER_SPACE) &&
            file.name.orEmpty().startsWith(BackupConfig.BACKUP_FILE_PREFIX) &&
            file.name.orEmpty().endsWith(BackupConfig.BACKUP_FILE_EXTENSION)
        if (!isPepLogBackup) {
            throw SecurityException("The selected Drive file is not a PepLog backup")
        }
    }
}
