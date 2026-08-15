package com.appvexis.peptidetracker.feature.pkcurves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.pkcurves.engine.PKEngine
import com.appvexis.peptidetracker.feature.pkcurves.model.CompoundCurveData
import com.appvexis.peptidetracker.feature.pkcurves.model.PKChartColors
import com.appvexis.peptidetracker.feature.pkcurves.model.PKVisualizerUiState
import com.appvexis.peptidetracker.feature.pkcurves.model.TimeWindow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the PK Half-Life Visualizer.
 *
 * Loads active protocols + their compounds + historical dose logs,
 * resolves peptide half-life data, and drives the PKEngine to compute
 * superimposed decay curves for each compound.
 */
@HiltViewModel
class PKVisualizerViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val logRepository: LogRepository,
    private val peptideRepository: PeptideRepository
) : ViewModel() {

    private val _timeWindow = MutableStateFlow(TimeWindow.DAYS_7)
    private val _visibilityMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _animationProgress = MutableStateFlow(0f)
    private val _crosshairTime = MutableStateFlow<Double?>(null)

    // Dose logs: use a very wide window to capture all relevant doses
    // (30 days back + 10 half-lives of the longest compound)
    private val nowMs = System.currentTimeMillis()
    private val thirtyDaysAgoMs = nowMs - (30L * 24 * 60 * 60 * 1000)

    val uiState: StateFlow<PKVisualizerUiState> = combine(
        protocolRepository.getActiveProtocols(),
        logRepository.getDoseLogs(thirtyDaysAgoMs, nowMs),
        peptideRepository.getAllPeptides(),
        _timeWindow,
        _visibilityMap
    ) { protocols, doseLogs, peptides, timeWindow, visibility ->
        computeState(protocols, doseLogs, peptides, timeWindow, visibility)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PKVisualizerUiState()
    )

    /**
     * Core computation: merge protocol/compound/dose data and run PK engine.
     */
    private fun computeState(
        protocols: List<ProtocolWithCompounds>,
        allDoseLogs: List<DoseLog>,
        allPeptides: List<Peptide>,
        timeWindow: TimeWindow,
        visibilityMap: Map<String, Boolean>
    ): PKVisualizerUiState {
        val peptideMap = allPeptides.associateBy { it.id }
        val doseLogsByCompound = allDoseLogs
            .filter { it.status == DoseStatus.TAKEN && it.actualTime != null }
            .groupBy { it.protocolCompoundId }

        val nowHours = nowMs.toDouble() / (1000.0 * 3600.0) // epoch hours
        val windowEndHours = nowHours
        val windowStartHours = nowHours - timeWindow.hours

        val compoundCurves = mutableListOf<CompoundCurveData>()
        var colorIndex = 0

        for (protocol in protocols) {
            for (compound in protocol.compounds) {
                val peptide = peptideMap[compound.peptideId]
                val halfLife = peptide?.halfLifeHours ?: continue // Skip compounds with no PK data

                val compoundDoseLogs = doseLogsByCompound[compound.id] ?: emptyList()
                if (compoundDoseLogs.isEmpty()) continue

                // Convert dose timestamps to epoch-hours
                val doseTimesHours = compoundDoseLogs.mapNotNull { log ->
                    log.actualTime?.let { it.toDouble() / (1000.0 * 3600.0) }
                }
                val doseAmounts = compoundDoseLogs.map { it.doseAmount }

                // Compute the PK curve
                val curvePoints = PKEngine.computeSuperpositionCurve(
                    doseTimesHours = doseTimesHours,
                    doseAmounts = doseAmounts,
                    halfLifeHours = halfLife,
                    windowStartHours = windowStartHours,
                    windowEndHours = windowEndHours,
                    resolution = 500
                )

                // Detect peaks and troughs
                val markers = PKEngine.detectMarkers(curvePoints)

                // Current level
                val currentLevel = PKEngine.concentrationAt(
                    doseTimesHours = doseTimesHours,
                    doseAmounts = doseAmounts,
                    halfLifeHours = halfLife,
                    timeHours = nowHours
                )

                val isVisible = visibilityMap.getOrDefault(compound.id, true)
                val doseUnitDisplay = compound.doseUnit.name.lowercase()

                compoundCurves.add(
                    CompoundCurveData(
                        compoundId = compound.id,
                        peptideId = compound.peptideId,
                        peptideName = peptide.name,
                        halfLifeHours = halfLife,
                        doseAmountMg = compound.doseAmount,
                        doseUnit = doseUnitDisplay,
                        color = PKChartColors.getColor(colorIndex),
                        points = curvePoints,
                        markers = markers,
                        currentLevel = currentLevel,
                        isVisible = isVisible
                    )
                )
                colorIndex++
            }
        }

        val maxConc = PKEngine.maxConcentration(
            compoundCurves.filter { it.isVisible }.map { it.points }
        )

        return PKVisualizerUiState(
            isLoading = false,
            activeTimeWindow = timeWindow,
            compounds = compoundCurves,
            hasData = compoundCurves.isNotEmpty(),
            animationProgress = _animationProgress.value,
            crosshairTimeHours = _crosshairTime.value,
            maxConcentration = maxConc * 1.1 // 10% headroom
        )
    }

    fun setTimeWindow(window: TimeWindow) {
        _timeWindow.value = window
        // Reset animation for fresh reveal
        _animationProgress.value = 0f
    }

    fun toggleCompoundVisibility(compoundId: String) {
        _visibilityMap.update { map ->
            val current = map.getOrDefault(compoundId, true)
            map + (compoundId to !current)
        }
    }

    fun updateAnimationProgress(progress: Float) {
        _animationProgress.value = progress
    }

    fun updateCrosshairTime(timeHours: Double?) {
        _crosshairTime.value = timeHours
    }
}
