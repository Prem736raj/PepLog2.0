package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.BiomarkerLog
import com.appvexis.peptidetracker.core.model.CalculatorPreset
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.InjectionSiteLog
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.model.SideEffectLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on tracking logs (doses, side effects, labs, photos, presets).
 */
interface LogRepository {
    // Dose logs
    fun getDoseLogs(startDate: Long, endDate: Long): Flow<List<DoseLog>>
    fun getDoseLogsForCompound(compoundId: String): Flow<List<DoseLog>>
    fun getPendingDoseLogs(currentTime: Long): Flow<List<DoseLog>>
    suspend fun insertDoseLog(log: DoseLog)
    suspend fun updateDoseLog(log: DoseLog)
    suspend fun deleteDoseLog(id: String)
    suspend fun logDoseTaken(id: String, actualTime: Long, site: String?, side: String?)

    // Injection Site logs
    fun getInjectionSiteLogs(): Flow<List<InjectionSiteLog>>
    fun getInjectionSiteLogsByArea(bodyArea: String): Flow<List<InjectionSiteLog>>
    fun getRecentInjectionSiteLogs(sinceTimestamp: Long): Flow<List<InjectionSiteLog>>
    suspend fun getInjectionSiteFrequency(sinceTimestamp: Long): Map<String, Int>
    suspend fun insertSiteLog(log: InjectionSiteLog)
    suspend fun updateSiteLog(log: InjectionSiteLog)
    suspend fun deleteSiteLog(id: String)

    // Side Effect logs
    fun getSideEffectLogs(): Flow<List<SideEffectLog>>
    fun getSideEffectLogsForProtocol(protocolId: String): Flow<List<SideEffectLog>>
    suspend fun insertSideEffectLog(log: SideEffectLog)
    suspend fun deleteSideEffectLog(id: String)

    // Biomarker logs
    fun getBiomarkerLogs(): Flow<List<BiomarkerLog>>
    fun getBiomarkerLogsForProtocol(protocolId: String): Flow<List<BiomarkerLog>>
    suspend fun insertBiomarkerLog(log: BiomarkerLog)
    suspend fun deleteBiomarkerLog(id: String)

    // Progress Photos
    fun getProgressPhotos(): Flow<List<ProgressPhoto>>
    fun getProgressPhotosForProtocol(protocolId: String): Flow<List<ProgressPhoto>>
    suspend fun insertProgressPhoto(photo: ProgressPhoto)
    suspend fun deleteProgressPhoto(id: String)

    // Calculator Presets
    fun getCalculatorPresets(): Flow<List<CalculatorPreset>>
    suspend fun insertCalculatorPreset(preset: CalculatorPreset)
    suspend fun deleteCalculatorPreset(id: String)
}
