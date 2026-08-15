package com.appvexis.peptidetracker.feature.reports

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.analytics.AnalyticsEngine
import com.appvexis.peptidetracker.core.model.BiomarkerLog
import com.appvexis.peptidetracker.core.model.DailyAnalyticsSummary
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolAnalyticsSummary
import com.appvexis.peptidetracker.core.model.SideEffectLog
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.reports.model.AdherenceSummaryUiModel
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerDeltaSummary
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerPoint
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerReportsUiModel
import com.appvexis.peptidetracker.feature.reports.model.BiomarkerSeriesUiModel
import com.appvexis.peptidetracker.feature.reports.model.DailyAdherencePoint
import com.appvexis.peptidetracker.feature.reports.model.DailySeverityPoint
import com.appvexis.peptidetracker.feature.reports.model.DailyWellnessPoint
import com.appvexis.peptidetracker.feature.reports.model.ProtocolComparisonCardModel
import com.appvexis.peptidetracker.feature.reports.model.ProtocolComparisonUiModel
import com.appvexis.peptidetracker.feature.reports.model.ReportsTab
import com.appvexis.peptidetracker.feature.reports.model.ReportsUiState
import com.appvexis.peptidetracker.feature.reports.model.SideEffectCategoryCount
import com.appvexis.peptidetracker.feature.reports.model.SideEffectItemUiModel
import com.appvexis.peptidetracker.feature.reports.model.SideEffectSummaryUiModel
import com.appvexis.peptidetracker.feature.reports.model.TimeRangeFilter
import com.appvexis.peptidetracker.feature.reports.model.WellnessMetricScore
import com.appvexis.peptidetracker.feature.reports.model.WellnessTrendUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val SymptomColors = listOf(
    Color(0xFFF43F5E), // Rose
    Color(0xFFFBBF24), // Amber
    Color(0xFF22D3EE), // Cyan
    Color(0xFFA78BFA), // Violet
    Color(0xFFFF4081), // Pink
    Color(0xFF34D399), // Emerald
    Color(0xFF38BDF8)  // Sky Blue
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val analyticsEngine: AnalyticsEngine,
    private val protocolRepository: ProtocolRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _selectedProtocolId = MutableStateFlow<String?>(null)
    private val _selectedTab = MutableStateFlow(ReportsTab.ADHERENCE)
    private val _selectedTimeRange = MutableStateFlow(TimeRangeFilter.DAYS_30)
    private val _selectedBiomarker = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    init {
        // Initial background recompute to ensure up-to-date summaries
        viewModelScope.launch {
            analyticsEngine.recomputeAll()
        }
    }

    private val filterStateFlow = combine(
        combine(
            protocolRepository.getAllProtocols(),
            _selectedProtocolId,
            _selectedTab
        ) { protocols, protocolId, tab ->
            Triple(protocols, protocolId, tab)
        },
        combine(
            _selectedTimeRange,
            _selectedBiomarker,
            _isRefreshing
        ) { timeRange, selectedBiomarker, isRefreshing ->
            Triple(timeRange, selectedBiomarker, isRefreshing)
        }
    ) { (protocols, protocolId, tab), (timeRange, selectedBiomarker, isRefreshing) ->
        ReportsFilterState(protocols, protocolId, tab, timeRange, selectedBiomarker, isRefreshing)
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        filterStateFlow,
        analyticsEngine.getAllDailySummaries(),
        analyticsEngine.getAllProtocolSummaries()
    ) { filter, dailySummaries, protocolSummaries ->
        Triple(filter, dailySummaries, protocolSummaries)
    }.combine(
        logRepository.getSideEffectLogs()
    ) { (filter, dailySummaries, protocolSummaries), sideEffectLogs ->
        Quadruple(filter, dailySummaries, protocolSummaries, sideEffectLogs)
    }.combine(
        logRepository.getBiomarkerLogs()
    ) { (filter, dailySummaries, protocolSummaries, sideEffectLogs), biomarkerLogs ->
        computeReportsUiState(
            filter = filter,
            allDailySummaries = dailySummaries,
            allProtocolSummaries = protocolSummaries,
            allSideEffectLogs = sideEffectLogs,
            allBiomarkerLogs = biomarkerLogs
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportsUiState()
    )


    private fun computeReportsUiState(
        filter: ReportsFilterState,
        allDailySummaries: List<DailyAnalyticsSummary>,
        allProtocolSummaries: List<ProtocolAnalyticsSummary>,
        allSideEffectLogs: List<SideEffectLog>,
        allBiomarkerLogs: List<BiomarkerLog>
    ): ReportsUiState {
        val protocols = filter.protocols
        val protocolId = filter.protocolId
        val timeRange = filter.timeRange

        val now = System.currentTimeMillis()
        val rangeCutoff = now - (timeRange.days * 24L * 60 * 60 * 1000)

        // 1. Filter Daily Summaries
        val filteredDaily = allDailySummaries
            .filter { (protocolId == null || it.protocolId == protocolId) && it.date >= rangeCutoff }
            .sortedBy { it.date }

        // 2. Compute Adherence UI Data
        val adherenceData = computeAdherence(filteredDaily)

        // 3. Filter and Compute Side Effect UI Data
        val filteredSideEffects = allSideEffectLogs
            .filter { (protocolId == null || it.protocolId == protocolId) && it.date >= rangeCutoff }
        val sideEffectsData = computeSideEffects(filteredDaily, filteredSideEffects)

        // 4. Filter and Compute Biomarker UI Data
        val filteredBiomarkers = allBiomarkerLogs
            .filter { (protocolId == null || it.protocolId == protocolId) && it.date >= rangeCutoff }
        val biomarkersData = computeBiomarkers(filteredBiomarkers, filter.selectedBiomarker)

        // 5. Compute Wellness UI Data
        val wellnessData = computeWellness(filteredDaily)

        // 6. Compute Protocol Comparison UI Data
        val comparisonData = computeComparison(protocols, allProtocolSummaries, allDailySummaries, allSideEffectLogs)

        val hasAnyData = filteredDaily.isNotEmpty() || filteredSideEffects.isNotEmpty() || filteredBiomarkers.isNotEmpty()

        return ReportsUiState(
            isLoading = false,
            isRefreshing = filter.isRefreshing,
            allProtocols = protocols,
            selectedProtocolId = protocolId,
            selectedTab = filter.tab,
            selectedTimeRange = timeRange,
            adherenceData = adherenceData,
            sideEffectsData = sideEffectsData,
            biomarkersData = biomarkersData,
            wellnessData = wellnessData,
            comparisonData = comparisonData,
            hasAnyData = hasAnyData
        )
    }

    private fun computeAdherence(dailyList: List<DailyAnalyticsSummary>): AdherenceSummaryUiModel {
        if (dailyList.isEmpty()) return AdherenceSummaryUiModel()

        val totalScheduled = dailyList.sumOf { it.totalDosesScheduled }
        val totalTaken = dailyList.sumOf { it.totalDosesTaken }
        val totalMissed = dailyList.sumOf { it.totalDosesMissed }
        val overallPct = if (totalScheduled > 0) (totalTaken.toDouble() / totalScheduled) * 100.0 else 0.0

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val points = dailyList.map { item ->
            DailyAdherencePoint(
                timestamp = item.date,
                dayLabel = dateFormat.format(Date(item.date)),
                scheduled = item.totalDosesScheduled,
                taken = item.totalDosesTaken,
                missed = item.totalDosesMissed,
                adherencePercent = item.adherencePercentage
            )
        }

        // Streak computation: count continuous days backwards with taken >= scheduled
        var streak = 0
        val reversed = dailyList.sortedByDescending { it.date }
        for (day in reversed) {
            if (day.totalDosesTaken > 0 && day.totalDosesMissed == 0) {
                streak++
            } else if (day.totalDosesScheduled > 0) {
                break
            }
        }

        return AdherenceSummaryUiModel(
            overallPercentage = overallPct,
            totalScheduled = totalScheduled,
            totalTaken = totalTaken,
            totalMissed = totalMissed,
            currentStreakDays = streak,
            dailyPoints = points
        )
    }

    private fun computeSideEffects(
        dailyList: List<DailyAnalyticsSummary>,
        sideEffectLogs: List<SideEffectLog>
    ): SideEffectSummaryUiModel {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val sideEffectsOnly = sideEffectLogs.filter { it.type == "side_effect" }

        val totalEvents = sideEffectsOnly.size
        val mostCommon = sideEffectsOnly
            .mapNotNull { it.category }
            .groupBy { it }
            .maxByOrNull { it.value.size }?.key

        val avgSev = if (sideEffectsOnly.isNotEmpty()) {
            sideEffectsOnly.map { it.severity.toDouble() }.average()
        } else 0.0

        val dailyPoints = dailyList
            .filter { it.sideEffectCount > 0 || (it.avgPainLevel ?: 0.0) > 0.0 }
            .map { item ->
                DailySeverityPoint(
                    timestamp = item.date,
                    dayLabel = dateFormat.format(Date(item.date)),
                    avgSeverity = item.avgPainLevel ?: (item.sideEffectCount.toDouble().coerceAtMost(10.0)),
                    count = item.sideEffectCount
                )
            }

        // Category frequency counts
        val categories = sideEffectsOnly
            .mapNotNull { it.category }
            .groupBy { it }
            .entries
            .mapIndexed { index, entry ->
                SideEffectCategoryCount(
                    categoryName = entry.key,
                    count = entry.value.size,
                    percentage = if (totalEvents > 0) (entry.value.size.toDouble() / totalEvents) * 100.0 else 0.0,
                    color = SymptomColors[index % SymptomColors.size]
                )
            }
            .sortedByDescending { it.count }

        val recentList = sideEffectsOnly.take(10).map {
            SideEffectItemUiModel(
                id = it.id,
                date = it.date,
                category = it.category ?: "Symptom",
                severity = it.severity,
                notes = it.notes
            )
        }

        return SideEffectSummaryUiModel(
            totalEvents = totalEvents,
            mostCommon = mostCommon,
            avgSeverity = avgSev,
            dailySeverityPoints = dailyPoints,
            categoryCounts = categories,
            recentSymptoms = recentList
        )
    }

    private fun computeBiomarkers(
        biomarkerLogs: List<BiomarkerLog>,
        selectedBiomarkerName: String
    ): BiomarkerReportsUiModel {
        if (biomarkerLogs.isEmpty()) return BiomarkerReportsUiModel()

        val grouped = biomarkerLogs.groupBy { it.biomarkerName }
        val available = grouped.keys.toList().sorted()
        val activeName = if (selectedBiomarkerName.isNotEmpty() && selectedBiomarkerName in available) {
            selectedBiomarkerName
        } else {
            available.firstOrNull() ?: ""
        }

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val activeLogs = (grouped[activeName] ?: emptyList()).sortedBy { it.date }
        val activeSeries = if (activeLogs.isNotEmpty()) {
            val baseline = activeLogs.first().value
            val current = activeLogs.last().value
            val delta = current - baseline
            val deltaPct = if (baseline != 0.0) (delta / baseline) * 100.0 else 0.0
            val unit = activeLogs.first().unit

            val points = activeLogs.map {
                BiomarkerPoint(
                    timestamp = it.date,
                    dayLabel = dateFormat.format(Date(it.date)),
                    value = it.value
                )
            }

            BiomarkerSeriesUiModel(
                biomarkerName = activeName,
                unit = unit,
                baselineValue = baseline,
                currentValue = current,
                deltaValue = delta,
                deltaPercentage = deltaPct,
                points = points
            )
        } else null

        val allDeltas = grouped.map { (name, logs) ->
            val sorted = logs.sortedBy { it.date }
            val first = sorted.first().value
            val last = sorted.last().value
            val delta = last - first
            val deltaPct = if (first != 0.0) (delta / first) * 100.0 else 0.0
            BiomarkerDeltaSummary(
                name = name,
                unit = sorted.first().unit,
                startValue = first,
                currentValue = last,
                delta = delta,
                deltaPercent = deltaPct
            )
        }

        return BiomarkerReportsUiModel(
            availableBiomarkers = available,
            selectedBiomarker = activeName,
            activeSeries = activeSeries,
            allBiomarkerDeltas = allDeltas
        )
    }

    private fun computeWellness(dailyList: List<DailyAnalyticsSummary>): WellnessTrendUiModel {
        if (dailyList.isEmpty()) return WellnessTrendUiModel()

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val moodScores = dailyList.mapNotNull { it.avgMood }
        val energyScores = dailyList.mapNotNull { it.avgEnergy }
        val sleepScores = dailyList.mapNotNull { it.avgSleepQuality }
        val painScores = dailyList.mapNotNull { it.avgPainLevel }

        val moodAvg = if (moodScores.isNotEmpty()) moodScores.average() else 0.0
        val energyAvg = if (energyScores.isNotEmpty()) energyScores.average() else 0.0
        val sleepAvg = if (sleepScores.isNotEmpty()) sleepScores.average() else 0.0
        val painAvg = if (painScores.isNotEmpty()) painScores.average() else 0.0

        val moodDelta = if (moodScores.size > 1) moodScores.last() - moodScores.first() else 0.0
        val energyDelta = if (energyScores.size > 1) energyScores.last() - energyScores.first() else 0.0
        val sleepDelta = if (sleepScores.size > 1) sleepScores.last() - sleepScores.first() else 0.0
        val painDelta = if (painScores.size > 1) painScores.last() - painScores.first() else 0.0

        val points = dailyList.map { item ->
            DailyWellnessPoint(
                timestamp = item.date,
                dayLabel = dateFormat.format(Date(item.date)),
                mood = item.avgMood,
                energy = item.avgEnergy,
                sleep = item.avgSleepQuality,
                pain = item.avgPainLevel,
                libido = null
            )
        }

        return WellnessTrendUiModel(
            moodScore = moodAvg,
            energyScore = energyAvg,
            sleepScore = sleepAvg,
            painScore = painAvg,
            libidoScore = 0.0,
            moodDelta = moodDelta,
            energyDelta = energyDelta,
            sleepDelta = sleepDelta,
            painDelta = painDelta,
            libidoDelta = 0.0,
            dailyTrends = points
        )
    }

    private fun computeComparison(
        protocols: List<Protocol>,
        summaries: List<ProtocolAnalyticsSummary>,
        dailySummaries: List<DailyAnalyticsSummary>,
        sideEffectLogs: List<SideEffectLog>
    ): ProtocolComparisonUiModel {
        val summaryMap = summaries.associateBy { it.protocolId }
        val dailyMap = dailySummaries.groupBy { it.protocolId }

        val cardList = protocols.map { proto ->
            val summary = summaryMap[proto.id]
            val dailies = dailyMap[proto.id] ?: emptyList()

            val adherence = summary?.overallAdherence ?: 0.0
            val totalScheduled = summary?.totalDosesScheduled ?: dailies.sumOf { it.totalDosesScheduled }
            val totalTaken = summary?.totalDosesTaken ?: dailies.sumOf { it.totalDosesTaken }
            val totalDays = summary?.totalDays ?: maxOf(1, dailies.size)

            val sideEffects = sideEffectLogs.filter { it.protocolId == proto.id && it.type == "side_effect" }
            val avgSev = summary?.avgSideEffectSeverity ?: if (sideEffects.isNotEmpty()) sideEffects.map { it.severity.toDouble() }.average() else null

            val wellnessDelta = summary?.subjectiveTrends?.values?.sum() ?: 0.0

            val topShift = summary?.biomarkerChanges?.entries?.maxByOrNull { kotlin.math.abs(it.value) }?.let {
                Pair(it.key, it.value)
            }

            val highlights = mutableListOf<String>()
            if (adherence >= 90.0) highlights.add("Excellent Adherence (${String.format("%.0f", adherence)}%)")
            if (avgSev != null && avgSev <= 2.0) highlights.add("Minimal Side Effects")
            if (wellnessDelta > 1.0) highlights.add("Positive Wellness Impact")

            ProtocolComparisonCardModel(
                protocolId = proto.id,
                protocolName = proto.name,
                goal = proto.goal,
                isActive = proto.status.name == "ACTIVE",
                totalDays = totalDays,
                adherenceRate = adherence,
                totalDosesTaken = totalTaken,
                totalDosesScheduled = totalScheduled,
                sideEffectCount = sideEffects.size,
                avgSideEffectSeverity = avgSev,
                wellnessDelta = wellnessDelta,
                topBiomarkerShift = topShift,
                highlights = highlights
            )
        }

        val bestAdherence = cardList.maxByOrNull { it.adherenceRate }?.takeIf { it.adherenceRate > 0 }?.protocolName
        val lowestSideEffects = cardList.filter { it.avgSideEffectSeverity != null }.minByOrNull { it.avgSideEffectSeverity ?: 10.0 }?.protocolName
        val highestWellness = cardList.maxByOrNull { it.wellnessDelta }?.takeIf { it.wellnessDelta > 0 }?.protocolName

        return ProtocolComparisonUiModel(
            protocols = cardList,
            bestAdherenceProtocol = bestAdherence,
            lowestSideEffectProtocol = lowestSideEffects,
            highestWellnessProtocol = highestWellness
        )
    }

    fun selectProtocol(protocolId: String?) {
        _selectedProtocolId.value = protocolId
    }

    fun selectTab(tab: ReportsTab) {
        _selectedTab.value = tab
    }

    fun selectTimeRange(range: TimeRangeFilter) {
        _selectedTimeRange.value = range
    }

    fun selectBiomarker(name: String) {
        _selectedBiomarker.value = name
    }

    fun recomputeAnalytics() {
        viewModelScope.launch {
            _isRefreshing.value = true
            analyticsEngine.recomputeAll()
            _isRefreshing.value = false
        }
    }
}

private data class ReportsFilterState(
    val protocols: List<Protocol>,
    val protocolId: String?,
    val tab: ReportsTab,
    val timeRange: TimeRangeFilter,
    val selectedBiomarker: String,
    val isRefreshing: Boolean
)

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
