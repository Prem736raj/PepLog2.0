package com.appvexis.peptidetracker.core.backup

/**
 * Central configuration for backup & export operations.
 */
object BackupConfig {

    // ---- Google Drive Folder ---- //
    const val DRIVE_FOLDER_NAME = "PepLog Backups"
    const val DRIVE_APP_FOLDER_SPACE = "appDataFolder" // Hidden app-specific folder

    // ---- Backup File Names ---- //
    const val BACKUP_FILE_PREFIX = "peplog_backup_"
    const val BACKUP_FILE_EXTENSION = ".json"
    const val DATABASE_BACKUP_NAME = "peplog_database.db"

    // ---- Export File Names ---- //
    const val EXPORT_DOSE_LOGS = "peplog_dose_logs.csv"
    const val EXPORT_PROTOCOLS = "peplog_protocols.csv"
    const val EXPORT_BIOMARKERS = "peplog_biomarkers.csv"
    const val EXPORT_INVENTORY = "peplog_inventory.csv"
    const val EXPORT_INJECTION_SITES = "peplog_injection_sites.csv"
    const val EXPORT_HEALTH_METRICS = "peplog_health_metrics.csv"
    const val EXPORT_FULL_JSON = "peplog_full_export.json"

    // ---- MIME Types ---- //
    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_CSV = "text/csv"
    const val MIME_TYPE_DB = "application/x-sqlite3"
    const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"

    // ---- WorkManager ---- //
    const val BACKUP_WORK_NAME = "peplog_auto_backup"
    const val BACKUP_INTERVAL_HOURS = 24L // Daily auto-backup

    // ---- DataStore Preference Keys ---- //
    const val PREF_LAST_BACKUP_TIME = "last_backup_time_ms"
    const val PREF_LAST_BACKUP_SIZE = "last_backup_size_bytes"
    const val PREF_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val PREF_GOOGLE_ACCOUNT_EMAIL = "google_account_email"

    // ---- Limits ---- //
    const val MAX_BACKUP_HISTORY = 5 // Keep last 5 backups on Drive
}
