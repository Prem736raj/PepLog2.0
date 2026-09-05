package com.appvexis.peptidetracker.core.backup

/**
 * Central configuration for backup & export operations.
 */
object BackupConfig {

    // ---- Export File Names ---- //
    const val EXPORT_DOSE_LOGS = "peplog_dose_logs.csv"
    const val EXPORT_PROTOCOLS = "peplog_protocols.csv"
    const val EXPORT_BIOMARKERS = "peplog_biomarkers.csv"
    const val EXPORT_INVENTORY = "peplog_inventory.csv"
    const val EXPORT_INJECTION_SITES = "peplog_injection_sites.csv"
    const val EXPORT_HEALTH_METRICS = "peplog_health_metrics.csv"
    const val EXPORT_FULL_JSON = "peplog_full_export.json"
    const val EXPORT_FULL_ARCHIVE = "peplog_backup.zip"
    const val EXPORT_CLINICIAN_REPORT = "peplog_clinician_summary.pdf"
    const val ARCHIVE_JSON_ENTRY = "backup.json"
    const val ARCHIVE_PHOTO_DIRECTORY = "photos/"

    // ---- MIME Types ---- //
    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_CSV = "text/csv"
    const val MIME_TYPE_ARCHIVE = "application/zip"
    const val MIME_TYPE_PDF = "application/pdf"
}
