package com.appvexis.peptidetracker.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.BiomarkerLog
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.SideEffectLog
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerCategory
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerCatalog
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerUiModel
import com.appvexis.peptidetracker.feature.progress.model.BodyMetricUiModel
import com.appvexis.peptidetracker.feature.progress.model.DailySubjectiveCheckIn
import com.appvexis.peptidetracker.feature.progress.model.PhotoCategory
import com.appvexis.peptidetracker.feature.progress.model.ProgressTab
import com.appvexis.peptidetracker.feature.progress.model.ProgressUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(ProgressTab.PHOTOS)
    val activeTab: StateFlow<ProgressTab> = _activeTab

    private val _selectedPhotoCategory = MutableStateFlow(PhotoCategory.ALL)
    val selectedPhotoCategory: StateFlow<PhotoCategory> = _selectedPhotoCategory

    private val _showAddPhotoDialog = MutableStateFlow(false)
    val showAddPhotoDialog: StateFlow<Boolean> = _showAddPhotoDialog

    private val _showLogBiomarkerDialog = MutableStateFlow(false)
    val showLogBiomarkerDialog: StateFlow<Boolean> = _showLogBiomarkerDialog

    private val _showLogSideEffectDialog = MutableStateFlow(false)
    val showLogSideEffectDialog: StateFlow<Boolean> = _showLogSideEffectDialog

    private val _showLogBodyMetricsDialog = MutableStateFlow(false)
    val showLogBodyMetricsDialog: StateFlow<Boolean> = _showLogBodyMetricsDialog

    val allProtocols: StateFlow<List<Protocol>> = protocolRepository.getAllProtocols()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val logsDataFlow = combine(
        logRepository.getProgressPhotos(),
        logRepository.getBiomarkerLogs(),
        logRepository.getSideEffectLogs()
    ) { photos, biomarkers, sideEffects ->
        Triple(photos, biomarkers, sideEffects)
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        _activeTab,
        _selectedPhotoCategory,
        logsDataFlow,
        allProtocols
    ) { tab, category, (photos, biomarkers, sideEffects), protocols ->
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        // 1. Map Biomarker logs to UI models
        val biomarkerUiList = biomarkers.filter { !it.biomarkerName.startsWith("BODY_") }
            .sortedByDescending { it.date }
            .map { log ->
                val def = BiomarkerCatalog.findDefinition(log.biomarkerName)
                val cat = def?.category ?: BiomarkerCategory.OTHER
                val rangeLow = def?.rangeLow
                val rangeHigh = def?.rangeHigh

                val status = when {
                    rangeLow != null && log.value < rangeLow -> "Low"
                    rangeHigh != null && log.value > rangeHigh -> "High"
                    rangeLow != null && rangeHigh != null -> "Optimal"
                    else -> "Logged"
                }

                BiomarkerUiModel(
                    log = log,
                    category = cat,
                    rangeLow = rangeLow,
                    rangeHigh = rangeHigh,
                    formattedDate = dateFormat.format(Date(log.date)),
                    statusText = status,
                    isOptimal = status == "Optimal"
                )
            }

        // 2. Map Subjective logs (mood, energy, sleep, pain, libido, side effects)
        val subjectiveByDay = sideEffects.groupBy { getStartOfDay(it.date) }
        val checkInList = subjectiveByDay.map { (dayStart, logs) ->
            val mood = logs.firstOrNull { it.type == "mood" }?.severity
            val energy = logs.firstOrNull { it.type == "energy" }?.severity
            val sleep = logs.firstOrNull { it.type == "sleep" }?.severity
            val pain = logs.firstOrNull { it.type == "pain" }?.severity
            val libido = logs.firstOrNull { it.type == "libido" }?.severity
            val adverseEffects = logs.filter { it.type == "side_effect" }
            val notes = logs.firstOrNull { !it.notes.isNullOrBlank() }?.notes

            DailySubjectiveCheckIn(
                date = dayStart,
                formattedDate = dateFormat.format(Date(dayStart)),
                mood = mood,
                energy = energy,
                sleep = sleep,
                pain = pain,
                libido = libido,
                sideEffects = adverseEffects,
                notes = notes
            )
        }.sortedByDescending { it.date }

        val latestCheckIn = checkInList.firstOrNull()

        // 3. Map Body metrics (stored in biomarker log table with prefix BODY_)
        val bodyMetricLogs = biomarkers.filter { it.biomarkerName.startsWith("BODY_") }
            .groupBy { getStartOfDay(it.date) }

        val bodyMetricsUiList = bodyMetricLogs.map { (dayStart, logs) ->
            val weight = logs.firstOrNull { it.biomarkerName == "BODY_WEIGHT" }?.value
            val fat = logs.firstOrNull { it.biomarkerName == "BODY_FAT" }?.value
            val muscle = logs.firstOrNull { it.biomarkerName == "BODY_MUSCLE" }?.value
            val waist = logs.firstOrNull { it.biomarkerName == "BODY_WAIST" }?.value
            val notes = logs.firstOrNull { !it.notes.isNullOrBlank() }?.notes

            BodyMetricUiModel(
                date = dayStart,
                formattedDate = dateFormat.format(Date(dayStart)),
                weightKg = weight,
                bodyFatPercent = fat,
                muscleMassKg = muscle,
                waistCm = waist,
                deltaWeightKg = null,
                notes = notes
            )
        }.sortedBy { it.date }

        // Compute delta weights
        val calculatedMetricsWithDeltas = bodyMetricsUiList.mapIndexed { index, item ->
            val prev = if (index > 0) bodyMetricsUiList[index - 1].weightKg else null
            val delta = if (item.weightKg != null && prev != null) item.weightKg - prev else null
            item.copy(deltaWeightKg = delta)
        }.reversed()

        val latestWeight = calculatedMetricsWithDeltas.firstOrNull { it.weightKg != null }?.weightKg
        val earliestWeight = calculatedMetricsWithDeltas.lastOrNull { it.weightKg != null }?.weightKg
        val totalWeightDelta = if (latestWeight != null && earliestWeight != null && latestWeight != earliestWeight) {
            latestWeight - earliestWeight
        } else null

        ProgressUiState.Success(
            activeTab = tab,
            photos = photos.sortedByDescending { it.date },
            selectedCategory = category,
            biomarkerLogs = biomarkerUiList,
            subjectiveCheckIns = checkInList,
            latestCheckIn = latestCheckIn,
            bodyMetrics = calculatedMetricsWithDeltas,
            latestWeight = latestWeight,
            weightDeltaTotal = totalWeightDelta,
            activeProtocols = protocols
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState.Loading
    )

    fun selectTab(tab: ProgressTab) {
        _activeTab.update { tab }
    }

    fun selectPhotoCategory(category: PhotoCategory) {
        _selectedPhotoCategory.update { category }
    }

    // Photo actions
    fun showAddPhotoDialog() { _showAddPhotoDialog.update { true } }
    fun hideAddPhotoDialog() { _showAddPhotoDialog.update { false } }

    fun addProgressPhoto(photoUri: String, category: String, protocolId: String?, notes: String?) {
        viewModelScope.launch {
            val photo = ProgressPhoto(
                id = UUID.randomUUID().toString(),
                photoUri = photoUri,
                date = System.currentTimeMillis(),
                protocolId = protocolId,
                category = category,
                notes = notes
            )
            logRepository.insertProgressPhoto(photo)
            hideAddPhotoDialog()
        }
    }

    fun deleteProgressPhoto(photoId: String) {
        viewModelScope.launch {
            logRepository.deleteProgressPhoto(photoId)
        }
    }

    // Biomarker actions
    fun showLogBiomarkerDialog() { _showLogBiomarkerDialog.update { true } }
    fun hideLogBiomarkerDialog() { _showLogBiomarkerDialog.update { false } }

    fun logBiomarker(name: String, value: Double, unit: String, protocolId: String?, lab: String?, notes: String?) {
        viewModelScope.launch {
            val biomarker = BiomarkerLog(
                id = UUID.randomUUID().toString(),
                biomarkerName = name,
                value = value,
                unit = unit,
                date = System.currentTimeMillis(),
                protocolId = protocolId,
                labName = lab,
                notes = notes
            )
            logRepository.insertBiomarkerLog(biomarker)
            hideLogBiomarkerDialog()
        }
    }

    fun deleteBiomarker(id: String) {
        viewModelScope.launch {
            logRepository.deleteBiomarkerLog(id)
        }
    }

    // Subjective wellness actions
    fun showLogSideEffectDialog() { _showLogSideEffectDialog.update { true } }
    fun hideLogSideEffectDialog() { _showLogSideEffectDialog.update { false } }

    fun saveDailySubjectiveCheckIn(
        mood: Int,
        energy: Int,
        sleep: Int,
        pain: Int,
        libido: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val metrics = listOf(
                "mood" to mood,
                "energy" to energy,
                "sleep" to sleep,
                "pain" to pain,
                "libido" to libido
            )

            metrics.forEach { (type, rating) ->
                val log = SideEffectLog(
                    id = UUID.randomUUID().toString(),
                    protocolId = null,
                    date = now,
                    type = type,
                    category = null,
                    severity = rating,
                    notes = notes,
                    createdAt = now
                )
                logRepository.insertSideEffectLog(log)
            }
        }
    }

    fun logSideEffect(category: String, severity: Int, protocolId: String?, notes: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val log = SideEffectLog(
                id = UUID.randomUUID().toString(),
                protocolId = protocolId,
                date = now,
                type = "side_effect",
                category = category,
                severity = severity,
                notes = notes,
                createdAt = now
            )
            logRepository.insertSideEffectLog(log)
            hideLogSideEffectDialog()
        }
    }

    fun deleteSideEffect(id: String) {
        viewModelScope.launch {
            logRepository.deleteSideEffectLog(id)
        }
    }

    // Body metrics actions
    fun showLogBodyMetricsDialog() { _showLogBodyMetricsDialog.update { true } }
    fun hideLogBodyMetricsDialog() { _showLogBodyMetricsDialog.update { false } }

    fun logBodyMetrics(
        weightKg: Double,
        bodyFat: Double?,
        muscleMass: Double?,
        waistCm: Double?,
        protocolId: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            logRepository.insertBiomarkerLog(
                BiomarkerLog(
                    id = UUID.randomUUID().toString(),
                    biomarkerName = "BODY_WEIGHT",
                    value = weightKg,
                    unit = "kg",
                    date = now,
                    protocolId = protocolId,
                    labName = null,
                    notes = notes
                )
            )

            bodyFat?.let { fat ->
                logRepository.insertBiomarkerLog(
                    BiomarkerLog(
                        id = UUID.randomUUID().toString(),
                        biomarkerName = "BODY_FAT",
                        value = fat,
                        unit = "%",
                        date = now,
                        protocolId = protocolId,
                        labName = null,
                        notes = notes
                    )
                )
            }

            muscleMass?.let { muscle ->
                logRepository.insertBiomarkerLog(
                    BiomarkerLog(
                        id = UUID.randomUUID().toString(),
                        biomarkerName = "BODY_MUSCLE",
                        value = muscle,
                        unit = "kg",
                        date = now,
                        protocolId = protocolId,
                        labName = null,
                        notes = notes
                    )
                )
            }

            waistCm?.let { waist ->
                logRepository.insertBiomarkerLog(
                    BiomarkerLog(
                        id = UUID.randomUUID().toString(),
                        biomarkerName = "BODY_WAIST",
                        value = waist,
                        unit = "cm",
                        date = now,
                        protocolId = protocolId,
                        labName = null,
                        notes = notes
                    )
                )
            }

            hideLogBodyMetricsDialog()
        }
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
