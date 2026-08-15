package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appvexis.peptidetracker.core.database.entity.BiomarkerLogEntity
import com.appvexis.peptidetracker.core.database.entity.CalculatorPresetEntity
import com.appvexis.peptidetracker.core.database.entity.DoseLogEntity
import com.appvexis.peptidetracker.core.database.entity.InjectionSiteLogEntity
import com.appvexis.peptidetracker.core.database.entity.ProgressPhotoEntity
import com.appvexis.peptidetracker.core.database.entity.SideEffectLogEntity
import com.appvexis.peptidetracker.core.database.entity.SiteFrequency
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for logging operations (Doses, Sites, Biomarkers, Photos, Symptoms, Presets).
 */
@Dao
interface LogDao {
    // Dose logs
    @Query("SELECT * FROM dose_log WHERE scheduled_time >= :startDate AND scheduled_time <= :endDate ORDER BY scheduled_time ASC")
    fun getDoseLogs(startDate: Long, endDate: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_log WHERE protocol_compound_id = :compoundId ORDER BY scheduled_time ASC")
    fun getDoseLogsForCompound(compoundId: String): Flow<List<DoseLogEntity>>

    @Query("SELECT dl.* FROM dose_log dl INNER JOIN protocol_compound pc ON dl.protocol_compound_id = pc.id WHERE pc.protocol_id = :protocolId ORDER BY dl.scheduled_time ASC")
    fun getDoseLogsForProtocol(protocolId: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_log WHERE status = 'PENDING' AND scheduled_time <= :currentTime ORDER BY scheduled_time ASC")
    fun getPendingDoseLogs(currentTime: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_log WHERE id = :id LIMIT 1")
    suspend fun getDoseLogById(id: String): DoseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLog(log: DoseLogEntity)

    @Update
    suspend fun updateDoseLog(log: DoseLogEntity)

    @Query("DELETE FROM dose_log WHERE id = :id")
    suspend fun deleteDoseLog(id: String)

    @Query("UPDATE dose_log SET actual_time = :actualTime, status = 'TAKEN', injection_site = :site, injection_side = :side WHERE id = :id")
    suspend fun logDoseTaken(id: String, actualTime: Long, site: String?, side: String?)

    // Injection Site logs
    @Query("SELECT * FROM injection_site_log ORDER BY timestamp DESC")
    fun getInjectionSiteLogs(): Flow<List<InjectionSiteLogEntity>>

    @Query("SELECT * FROM injection_site_log WHERE body_area = :bodyArea ORDER BY timestamp DESC")
    fun getInjectionSiteLogsByArea(bodyArea: String): Flow<List<InjectionSiteLogEntity>>

    @Query("SELECT * FROM injection_site_log WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRecentInjectionSiteLogs(sinceTimestamp: Long): Flow<List<InjectionSiteLogEntity>>

    @Query("SELECT body_area, COUNT(*) as count FROM injection_site_log WHERE timestamp >= :sinceTimestamp GROUP BY body_area ORDER BY count DESC")
    suspend fun getInjectionSiteFrequency(sinceTimestamp: Long): List<SiteFrequency>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteLog(log: InjectionSiteLogEntity)

    @Update
    suspend fun updateSiteLog(log: InjectionSiteLogEntity)

    @Query("DELETE FROM injection_site_log WHERE id = :id")
    suspend fun deleteSiteLog(id: String)


    // Side Effect logs
    @Query("SELECT * FROM side_effect_log ORDER BY date DESC")
    fun getSideEffectLogs(): Flow<List<SideEffectLogEntity>>

    @Query("SELECT * FROM side_effect_log WHERE protocol_id = :protocolId ORDER BY date DESC")
    fun getSideEffectLogsForProtocol(protocolId: String): Flow<List<SideEffectLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSideEffectLog(log: SideEffectLogEntity)

    @Query("SELECT * FROM side_effect_log WHERE id = :id LIMIT 1")
    suspend fun getSideEffectLogById(id: String): SideEffectLogEntity?

    @Query("DELETE FROM side_effect_log WHERE id = :id")
    suspend fun deleteSideEffectLog(id: String)

    // Biomarker logs
    @Query("SELECT * FROM biomarker_log ORDER BY date DESC")
    fun getBiomarkerLogs(): Flow<List<BiomarkerLogEntity>>

    @Query("SELECT * FROM biomarker_log WHERE protocol_id = :protocolId ORDER BY date DESC")
    fun getBiomarkerLogsForProtocol(protocolId: String): Flow<List<BiomarkerLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiomarkerLog(log: BiomarkerLogEntity)

    @Query("SELECT * FROM biomarker_log WHERE id = :id LIMIT 1")
    suspend fun getBiomarkerLogById(id: String): BiomarkerLogEntity?

    @Query("DELETE FROM biomarker_log WHERE id = :id")
    suspend fun deleteBiomarkerLog(id: String)

    // Progress Photos
    @Query("SELECT * FROM progress_photo ORDER BY date DESC")
    fun getProgressPhotos(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT * FROM progress_photo WHERE protocol_id = :protocolId ORDER BY date DESC")
    fun getProgressPhotosForProtocol(protocolId: String): Flow<List<ProgressPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhotoEntity)

    @Query("SELECT * FROM progress_photo WHERE id = :id LIMIT 1")
    suspend fun getProgressPhotoById(id: String): ProgressPhotoEntity?

    @Query("DELETE FROM progress_photo WHERE id = :id")
    suspend fun deleteProgressPhoto(id: String)

    // Calculator Presets
    @Query("SELECT * FROM calculator_preset ORDER BY created_at DESC")
    fun getCalculatorPresets(): Flow<List<CalculatorPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculatorPreset(preset: CalculatorPresetEntity)

    @Query("DELETE FROM calculator_preset WHERE id = :id")
    suspend fun deleteCalculatorPreset(id: String)

    // Sync queries for backup export
    @Query("SELECT * FROM dose_log ORDER BY scheduled_time DESC")
    suspend fun getAllDoseLogsSync(): List<DoseLogEntity>

    @Query("SELECT * FROM injection_site_log ORDER BY timestamp DESC")
    suspend fun getAllInjectionSiteLogsSync(): List<InjectionSiteLogEntity>

    @Query("SELECT * FROM side_effect_log ORDER BY date DESC")
    suspend fun getAllSideEffectLogsSync(): List<SideEffectLogEntity>

    @Query("SELECT * FROM biomarker_log ORDER BY date DESC")
    suspend fun getAllBiomarkerLogsSync(): List<BiomarkerLogEntity>

    @Query("SELECT * FROM progress_photo ORDER BY date DESC")
    suspend fun getAllProgressPhotosSync(): List<ProgressPhotoEntity>

    @Query("SELECT * FROM calculator_preset ORDER BY created_at DESC")
    suspend fun getAllCalculatorPresetsSync(): List<CalculatorPresetEntity>
}
