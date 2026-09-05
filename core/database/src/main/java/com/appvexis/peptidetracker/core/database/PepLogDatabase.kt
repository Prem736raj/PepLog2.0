package com.appvexis.peptidetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appvexis.peptidetracker.core.database.converters.DatabaseConverters
import com.appvexis.peptidetracker.core.database.dao.AnalyticsDao
import com.appvexis.peptidetracker.core.database.dao.HealthMetricDao
import com.appvexis.peptidetracker.core.database.dao.InventoryDao
import com.appvexis.peptidetracker.core.database.dao.LogDao
import com.appvexis.peptidetracker.core.database.dao.PeptideDao
import com.appvexis.peptidetracker.core.database.dao.ProtocolDao
import com.appvexis.peptidetracker.core.database.entity.BiomarkerLogEntity
import com.appvexis.peptidetracker.core.database.entity.CalculatorPresetEntity
import com.appvexis.peptidetracker.core.database.entity.DailyAnalyticsSummaryEntity
import com.appvexis.peptidetracker.core.database.entity.DoseLogEntity
import com.appvexis.peptidetracker.core.database.entity.HealthMetricEntity
import com.appvexis.peptidetracker.core.database.entity.InjectionSiteLogEntity
import com.appvexis.peptidetracker.core.database.entity.InventoryItemEntity
import com.appvexis.peptidetracker.core.database.entity.PeptideEntity
import com.appvexis.peptidetracker.core.database.entity.ProgressPhotoEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolAnalyticsSummaryEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolCompoundEntity
import com.appvexis.peptidetracker.core.database.entity.ProtocolEntity
import com.appvexis.peptidetracker.core.database.entity.SideEffectLogEntity

/**
 * Main Room database configuration for offline-first PepLog peptide tracker.
 */
@Database(
    entities = [
        PeptideEntity::class,
        ProtocolEntity::class,
        ProtocolCompoundEntity::class,
        DoseLogEntity::class,
        InjectionSiteLogEntity::class,
        InventoryItemEntity::class,
        SideEffectLogEntity::class,
        BiomarkerLogEntity::class,
        ProgressPhotoEntity::class,
        CalculatorPresetEntity::class,
        DailyAnalyticsSummaryEntity::class,
        ProtocolAnalyticsSummaryEntity::class,
        HealthMetricEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class PepLogDatabase : RoomDatabase() {
    abstract fun peptideDao(): PeptideDao
    abstract fun protocolDao(): ProtocolDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun logDao(): LogDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun healthMetricDao(): HealthMetricDao
}
