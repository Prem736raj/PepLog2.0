package com.appvexis.peptidetracker.feature.pkcurves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
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

    val uiState: StateFlow<PKVisualizerUiState> = combine(
        protocolRepository.getActiveProtocols(),
        // The engine needs older doses for long half-lives. The database is already
        // local and schedule generation is bounded, so retaining the full history
        // here is more accurate than silently dropping doses after 30 days.
        logRepository.getDoseLogs(0L, Long.MAX_VALUE),
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
        val nowMs = System.currentTimeMillis()
        val nowHours = nowMs.toDouble() / (1000.0 * 3600.0) // epoch hours
        // Reserve part of the chart for history and part for a clearly marked
        // scheduled-dose forecast. This is still an elimination-only estimate.
        val historyHours = timeWindow.hours * 0.7
        val windowStartHours = nowHours - historyHours
        val windowEndHours = nowHours + (timeWindow.hours - historyHours)

        val compoundCurves = mutableListOf<CompoundCurveData>()
        var colorIndex = 0

        for (protocol in protocols) {
            for (compound in protocol.compounds) {
                val compoundDoseLogs = allDoseLogs
                    .filter { it.protocolCompoundId == compound.id }
                    .filter { log ->
                        log.status == DoseStatus.TAKEN && log.actualTime != null ||
                            log.status == DoseStatus.PENDING && log.scheduledTime >= nowMs
                    }
                if (compoundDoseLogs.isEmpty()) continue

                val peptide = peptideMap[compound.peptideId]
                val halfLife = peptide?.halfLifeHours
                    ?.takeIf { it.isFinite() && it > 0.0 }
                val hasIuDoses = compoundDoseLogs.any { it.doseUnit == DoseUnit.IU }

                if (halfLife == null) {
                    compoundCurves.add(
                        unavailableCurve(
                            compoundId = compound.id,
                            peptideId = compound.peptideId,
                            peptideName = peptide?.name ?: "Unknown compound",
                            doseAmount = compoundDoseLogs.last().doseAmount,
                            doseUnit = compoundDoseLogs.last().doseUnit.name,
                            color = PKChartColors.getColor(colorIndex),
                            reason = "No validated half-life is available for this compound."
                        )
                    )
                    colorIndex++
                    continue
                }

                val doseSamples = compoundDoseLogs.mapNotNull { log ->
                    val eventTime = when (log.status) {
                        DoseStatus.TAKEN -> log.actualTime
                        DoseStatus.PENDING -> log.scheduledTime.takeIf { it >= nowMs }
                        else -> null
                    } ?: return@mapNotNull null
                    val doseAmountMg = when (log.doseUnit) {
                        DoseUnit.MG -> log.doseAmount
                        DoseUnit.MCG -> log.doseAmount / 1000.0
                        // IU has no universal conversion to mass, so do not
                        // draw a fabricated mg-equivalent curve.
                        DoseUnit.IU -> return@mapNotNull null
                    }
                    if (!doseAmountMg.isFinite() || doseAmountMg <= 0.0) return@mapNotNull null
                    eventTime.toDouble() / (1000.0 * 3600.0) to doseAmountMg
                }.sortedBy { it.first }

                if (doseSamples.isEmpty()) {
                    compoundCurves.add(
                        unavailableCurve(
                            compoundId = compound.id,
                            peptideId = compound.peptideId,
                            peptideName = peptide?.name ?: "Unknown compound",
                            doseAmount = compoundDoseLogs.last().doseAmount,
                            doseUnit = compoundDoseLogs.last().doseUnit.name,
                            color = PKChartColors.getColor(colorIndex),
                            reason = "IU doses are recorded, but IU cannot be converted to mass safely."
                        )
                    )
                    colorIndex++
                    continue
                }
                val doseTimesHours = doseSamples.map { it.first }
                val doseAmounts = doseSamples.map { it.second }

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
                val doseUnitDisplay = "normalized mg"

                compoundCurves.add(
                    CompoundCurveData(
                        compoundId = compound.id,
                        peptideId = compound.peptideId,
                        peptideName = peptide.name,
                        halfLifeHours = halfLife,
                        doseAmount = doseAmounts.last(),
                        doseUnit = doseUnitDisplay,
                        color = PKChartColors.getColor(colorIndex),
                        points = curvePoints,
                        markers = markers,
                        currentLevel = currentLevel,
                        isVisible = isVisible,
                        dataWarning = if (hasIuDoses) {
                            "IU doses are not included because there is no universal IU-to-mass conversion."
                        } else null,
                        projectionStartHours = historyHours
                    )
                )
                colorIndex++
            }
        }

        val maxConc = PKEngine.maxConcentration(
            compoundCurves.filter { it.isPkAvailable && it.isVisible }.map { it.points }
        )

        return PKVisualizerUiState(
            isLoading = false,
            activeTimeWindow = timeWindow,
            compounds = compoundCurves,
            hasData = compoundCurves.any { it.isPkAvailable && it.points.isNotEmpty() },
            hasUnavailableData = compoundCurves.any { !it.isPkAvailable },
            animationProgress = _animationProgress.value,
            crosshairTimeHours = _crosshairTime.value,
            maxConcentration = maxConc * 1.1 // 10% headroom
        )
    }

    private fun unavailableCurve(
        compoundId: String,
        peptideId: String,
        peptideName: String,
        doseAmount: Double,
        doseUnit: String,
        color: androidx.compose.ui.graphics.Color,
        reason: String
    ) = CompoundCurveData(
        compoundId = compoundId,
        peptideId = peptideId,
        peptideName = peptideName,
        halfLifeHours = 0.0,
        doseAmount = doseAmount,
        doseUnit = doseUnit,
        color = color,
        points = emptyList(),
        markers = emptyList(),
        currentLevel = 0.0,
        isPkAvailable = false,
        unavailableReason = reason
    )

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
