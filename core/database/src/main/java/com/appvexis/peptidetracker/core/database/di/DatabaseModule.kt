package com.appvexis.peptidetracker.core.database.di

import android.content.Context
import androidx.room.Room
import com.appvexis.peptidetracker.core.database.PepLogDatabase
import com.appvexis.peptidetracker.core.database.dao.AnalyticsDao
import com.appvexis.peptidetracker.core.database.dao.DeviceDao
import com.appvexis.peptidetracker.core.database.dao.HealthMetricDao
import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.dao.LogDao
import com.appvexis.peptidetracker.core.database.dao.PeptideDao
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.repository.AnalyticsRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.DeviceRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.HealthRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.InventoryRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.LogRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.PeptideRepositoryImpl
import com.appvexis.peptidetracker.core.database.repository.ProtocolRepositoryImpl
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.DeviceRepository
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
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

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
            // NOTE: Do NOT use fallbackToDestructiveMigration() in production.
            // Any future schema changes must use explicit Migration objects
            // to preserve user data. See Room migration docs:
            // https://developer.android.com/training/data-storage/room/migrating-db-versions
            .build()
        }

        @Provides
        fun provideDeviceDao(db: PepLogDatabase): DeviceDao = db.deviceDao()

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
    }
}
