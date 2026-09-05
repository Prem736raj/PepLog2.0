package com.appvexis.peptidetracker.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appvexis.peptidetracker.core.database.PepLogDatabase
import com.appvexis.peptidetracker.core.database.dao.AnalyticsDao
import com.appvexis.peptidetracker.core.database.dao.HealthMetricDao
import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.dao.LogDao
import com.appvexis.peptidetracker.core.database.dao.PeptideDao
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.repository.AnalyticsRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.HealthRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.InventoryRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.LogRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.PeptideRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.ProtocolRepositoryImpl
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.HealthRepository
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for the data and storage layer.
 * Configures database builder and binds repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindPeptideRepository(impl: PeptideRepositoryImpl): PeptideRepository

    @Binds
    @Singleton
    abstract fun bindProtocolRepository(impl: ProtocolRepositoryImpl): ProtocolRepository

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindLogRepository(impl: LogRepositoryImpl): LogRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(
            @ApplicationContext context: Context
        ): PepLogDatabase {
            return Room.databaseBuilder(
                context,
                PepLogDatabase::class.java,
                "peplog.db"
            )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
            // NOTE: Do NOT use fallbackToDestructiveMigration() in production.
            // Any future schema changes must use explicit Migration objects
            // to preserve user data. See Room migration docs:
            // https://developer.android.com/training/data-storage/room/migrating-db-versions
            .build()
        }

        @Provides
        fun providePeptideDao(db: PepLogDatabase): PeptideDao = db.peptideDao()

        @Provides
        fun provideProtocolDao(db: PepLogDatabase): ProtocolDao = db.protocolDao()

        @Provides
        fun provideInventoryDao(db: PepLogDatabase): InventoryDao = db.inventoryDao()

        @Provides
        fun provideLogDao(db: PepLogDatabase): LogDao = db.logDao()

        @Provides
        fun provideAnalyticsDao(db: PepLogDatabase): AnalyticsDao = db.analyticsDao()

        @Provides
        fun provideHealthMetricDao(db: PepLogDatabase): HealthMetricDao = db.healthMetricDao()

        /** Removes the obsolete local device table without touching user records. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS device_info")
            }
        }

        /** Renames the inventory timestamp while preserving every stored vial record. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `inventory_item_new` (
                        `id` TEXT NOT NULL,
                        `peptide_id` TEXT NOT NULL,
                        `vendor` TEXT,
                        `batch_number` TEXT,
                        `acquired_date` INTEGER,
                        `vial_strength_mg` REAL NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `storage_location` TEXT,
                        `is_reconstituted` INTEGER NOT NULL,
                        `reconstitution_date` INTEGER,
                        `bac_water_ml` REAL,
                        `concentration_mg_ml` REAL,
                        `expiration_date` INTEGER,
                        `remaining_volume_ml` REAL,
                        `notes` TEXT,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`peptide_id`) REFERENCES `peptide`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `inventory_item_new` (
                        `id`, `peptide_id`, `vendor`, `batch_number`, `acquired_date`,
                        `vial_strength_mg`, `quantity`, `storage_location`, `is_reconstituted`,
                        `reconstitution_date`, `bac_water_ml`, `concentration_mg_ml`,
                        `expiration_date`, `remaining_volume_ml`, `notes`, `status`
                    )
                    SELECT
                        `id`, `peptide_id`, `vendor`, `batch_number`, `purchase_date`,
                        `vial_strength_mg`, `quantity`, `storage_location`, `is_reconstituted`,
                        `reconstitution_date`, `bac_water_ml`, `concentration_mg_ml`,
                        `expiration_date`, `remaining_volume_ml`, `notes`, `status`
                    FROM `inventory_item`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `inventory_item`")
                db.execSQL("ALTER TABLE `inventory_item_new` RENAME TO `inventory_item`")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_inventory_item_peptide_id` ON `inventory_item` (`peptide_id`)"
                )
            }
        }
    }
}
