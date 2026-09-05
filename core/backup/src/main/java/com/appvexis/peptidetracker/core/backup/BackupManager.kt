package com.appvexis.peptidetracker.core.backup

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.appvexis.peptidetracker.core.backup.export.CsvExporter
import com.appvexis.peptidetracker.core.backup.export.DatabaseExporter
import com.appvexis.peptidetracker.core.backup.export.PdfReportExporter
import com.appvexis.peptidetracker.core.backup.importer.DatabaseImporter
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
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
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
    private val pdfReportExporter: PdfReportExporter,
    private val databaseImporter: DatabaseImporter,
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

    /** Exports a concise clinician-facing record summary without progress photos. */
    suspend fun exportToPdf(outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.2f, "Preparing your records")
            val data = databaseExporter.exportToBackupData()
            _backupStatus.value = BackupStatus.InProgress(0.7f, "Writing PDF summary")
            val file = pdfReportExporter.export(data, outputFile)
            completeExport(file.length())
            Result.success(file)
        } catch (error: Exception) {
            failExport(error)
        }
    }

    /**
     * Exports JSON plus private progress-photo copies in one portable archive.
     * The archive is still local until the user chooses a destination/share app.
     */
    suspend fun exportToArchive(outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            _backupStatus.value = BackupStatus.InProgress(0.15f, "Preparing your records")
            val data = databaseExporter.exportToBackupData()
            outputFile.parentFile?.mkdirs()
            ZipOutputStream(FileOutputStream(outputFile).buffered()).use { archive ->
                archive.putNextEntry(ZipEntry(BackupConfig.ARCHIVE_JSON_ENTRY))
                archive.write(databaseExporter.toJson(data).toByteArray(Charsets.UTF_8))
                archive.closeEntry()

                data.progressPhotos.forEachIndexed { index, photo ->
                    _backupStatus.value = BackupStatus.InProgress(
                        progress = 0.2f + (0.7f * (index + 1).toFloat() / data.progressPhotos.size.coerceAtLeast(1)),
                        message = "Copying progress photos"
                    )
                    val extension = safePhotoExtension(photo.photoUri)
                    require(isSafeArchiveComponent(photo.id)) { "Progress photo has an invalid ID" }
                    archive.putNextEntry(ZipEntry("${BackupConfig.ARCHIVE_PHOTO_DIRECTORY}${photo.id}.$extension"))
                    openPhotoInput(photo.photoUri).use { input ->
                        require(input != null) { "Progress photo ${photo.id} is no longer available" }
                        copyBounded(input, archive, MAX_PHOTO_BYTES)
                    }
                    archive.closeEntry()
                }
            }
            completeExport(outputFile.length())
            Result.success(outputFile)
        } catch (error: Exception) {
            failExport(error)
        }
    }

    /** Restores a JSON export or a photo-inclusive PepLog ZIP archive atomically. */
    suspend fun importFromFile(inputFile: File): Result<Int> = withContext(Dispatchers.IO) {
        val restoredPhotos = mutableListOf<File>()
        try {
            require(inputFile.isFile) { "The selected backup file is unavailable" }
            _backupStatus.value = BackupStatus.InProgress(0.15f, "Checking backup")
            val recordCount = if (isZipFile(inputFile)) {
                importArchive(inputFile, restoredPhotos)
            } else {
                require(inputFile.length() <= MAX_BACKUP_JSON_BYTES) { "Backup file is too large" }
                val data = inputFile.readText(Charsets.UTF_8).let(databaseExporter::fromJson)
                databaseImporter.importBackup(data)
            }
            _backupStatus.value = BackupStatus.Restored(System.currentTimeMillis(), recordCount)
            Result.success(recordCount)
        } catch (error: Exception) {
            restoredPhotos.forEach { it.delete() }
            failExport(error)
        }
    }

    private suspend fun importArchive(inputFile: File, restoredPhotos: MutableList<File>): Int {
        require(inputFile.length() <= MAX_ARCHIVE_BYTES) { "Backup archive is too large" }
        ZipFile(inputFile).use { archive ->
            val entries = archive.entries().asSequence().toList()
            require(entries.size <= MAX_ARCHIVE_ENTRIES) { "Backup archive contains too many files" }
            require(entries.all {
                it.name == BackupConfig.ARCHIVE_JSON_ENTRY ||
                    it.name.startsWith(BackupConfig.ARCHIVE_PHOTO_DIRECTORY)
            }) { "Backup archive contains an unsupported file" }
            require(entries.count { it.name == BackupConfig.ARCHIVE_JSON_ENTRY } == 1) {
                "Backup archive must contain exactly one backup.json"
            }
            val jsonEntry = archive.getEntry(BackupConfig.ARCHIVE_JSON_ENTRY)
            require(jsonEntry != null && !jsonEntry.isDirectory) { "Backup archive is missing backup.json" }
            require(jsonEntry.size < 0L || jsonEntry.size <= MAX_BACKUP_JSON_BYTES) { "Backup metadata is too large" }
            val json = archive.getInputStream(jsonEntry).use { input ->
                val bytes = ByteArrayOutputStream()
                copyBounded(input, bytes, MAX_BACKUP_JSON_BYTES)
                bytes.toByteArray().toString(Charsets.UTF_8)
            }
            require(json.length <= MAX_BACKUP_JSON_BYTES) { "Backup metadata is too large" }
            val data = databaseExporter.fromJson(json)
            val expectedPhotoIds = data.progressPhotos.map { it.id }.toSet()
            val seenPhotoIds = mutableSetOf<String>()
            val photoUriOverrides = mutableMapOf<String, String>()

            entries.filter { it.name.startsWith(BackupConfig.ARCHIVE_PHOTO_DIRECTORY) }.forEach { entry ->
                require(!entry.isDirectory) { "Backup archive contains an invalid photo entry" }
                val fileName = entry.name.removePrefix(BackupConfig.ARCHIVE_PHOTO_DIRECTORY)
                val extension = fileName.substringAfterLast('.', "").lowercase()
                val photoId = fileName.substringBeforeLast('.', "")
                require(photoId in expectedPhotoIds && extension in SUPPORTED_PHOTO_EXTENSIONS) {
                    "Backup archive contains an unexpected photo entry"
                }
                require(seenPhotoIds.add(photoId)) { "Backup archive contains a duplicate photo" }
                require(entry.size < 0L || entry.size <= MAX_PHOTO_BYTES) { "Progress photo is too large" }

                val directory = File(context.filesDir, PHOTO_DIRECTORY_NAME).apply {
                    require(exists() || mkdirs()) { "Could not create private photo storage" }
                }
                val destination = File(directory, "restore-${UUID.randomUUID()}.$extension")
                // Register before copying so a truncated/invalid entry is also
                // removed by the outer failure cleanup.
                restoredPhotos += destination
                archive.getInputStream(entry).use { input ->
                    FileOutputStream(destination).use { output ->
                        copyBounded(input, output, MAX_PHOTO_BYTES)
                    }
                }
                photoUriOverrides[photoId] = Uri.fromFile(destination).toString()
            }

            require(seenPhotoIds == expectedPhotoIds) {
                "Backup archive is missing one or more progress photos"
            }
            return databaseImporter.importBackup(data, photoUriOverrides)
        }
    }

    private fun openPhotoInput(photoUri: String) = runCatching {
        val uri = Uri.parse(photoUri)
        if (uri.scheme.isNullOrBlank() || uri.scheme == "file") {
            val path = uri.path ?: photoUri
            val privateDirectory = File(context.filesDir, PHOTO_DIRECTORY_NAME).canonicalFile.toPath()
            val photoFile = File(path).canonicalFile
            require(photoFile.toPath().startsWith(privateDirectory)) {
                "Only PepLog private progress photos can be archived"
            }
            FileInputStream(photoFile)
        } else context.contentResolver.openInputStream(uri)
    }.getOrNull()

    private fun safePhotoExtension(photoUri: String): String {
        val extension = Uri.parse(photoUri).path
            ?.substringAfterLast('.', "")
            ?.lowercase()
        return extension?.takeIf { it in SUPPORTED_PHOTO_EXTENSIONS } ?: "bin"
    }

    private fun isSafeArchiveComponent(value: String): Boolean =
        value.isNotBlank() && value != "." && value != ".." &&
            '/' !in value && '\\' !in value && !value.contains('\u0000')

    private fun isZipFile(file: File): Boolean = runCatching {
        FileInputStream(file).use { input -> input.read() == 'P'.code && input.read() == 'K'.code }
    }.getOrDefault(false)

    private fun copyBounded(input: java.io.InputStream?, output: java.io.OutputStream, maxBytes: Long) {
        requireNotNull(input) { "The selected progress photo is unavailable" }
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= maxBytes) { "File exceeds the permitted backup size" }
            output.write(buffer, 0, count)
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

    /** Exposes failures from the file-picker copy step to the same status UI. */
    fun reportError(error: Throwable) {
        Timber.e(error, "BackupManager: import file could not be read")
        _backupStatus.value = BackupStatus.Error(error.message ?: "The backup could not be read")
    }

    private companion object {
        const val PHOTO_DIRECTORY_NAME = "progress_photos"
        const val MAX_PHOTO_BYTES = 25L * 1024L * 1024L
        const val MAX_ARCHIVE_BYTES = 200L * 1024L * 1024L
        const val MAX_ARCHIVE_ENTRIES = 100_000
        const val MAX_BACKUP_JSON_BYTES = 10L * 1024L * 1024L
        val SUPPORTED_PHOTO_EXTENSIONS = setOf("jpeg", "jpg", "png", "webp", "heic", "heif", "bin")
    }
}
