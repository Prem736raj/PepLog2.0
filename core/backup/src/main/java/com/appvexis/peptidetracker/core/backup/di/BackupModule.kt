package com.appvexis.peptidetracker.core.backup.di

import com.appvexis.peptidetracker.core.backup.BackupManager
import com.appvexis.peptidetracker.core.backup.drive.DriveBackupService
import com.appvexis.peptidetracker.core.backup.export.CsvExporter
import com.appvexis.peptidetracker.core.backup.export.DatabaseExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the backup subsystem.
 * All classes use constructor injection via @Inject @Singleton,
 * so this module primarily exists as a verification point.
 * Additional provides can be added here if needed for testing.
 */
@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    // All backup classes use @Inject constructor + @Singleton,
    // so Hilt discovers them automatically.
    // This module is kept for potential future @Provides methods
    // (e.g., providing a custom Json serializer, mock Drive service for tests).
}
