package com.appvexis.peptidetracker.feature.reports.model

import androidx.compose.ui.graphics.Color
import com.appvexis.peptidetracker.core.model.Protocol

/**
 * Navigation tabs inside the Insights & Analytics screen.
 */
enum class ReportsTab(val title: String) {
    ADHERENCE("Adherence"),
    SIDE_EFFECTS("Side Effects"),
    BIOMARKERS("Biomarkers"),
    WELLNESS("Wellness"),
    COMPARISON("Comparison")
}

/**
 * Filter window for analytical charts.
 */
enum class TimeRangeFilter(val label: String, val days: Int) {
    DAYS_7("7D", 7),
    DAYS_30("30D", 30),
    DAYS_90("90D", 90),
    ALL_TIME("All", 365)
}

// ----------------------------------------------------
// Adherence Models
// ----------------------------------------------------

data class DailyAdherencePoint(
    val timestamp: Long,
    val dayLabel: String,
    val scheduled: Int,
    val taken: Int,
    val missed: Int,
    val adherencePercent: Double
)

data class AdherenceSummaryUiModel(
    val overallPercentage: Double = 0.0,
    val totalScheduled: Int = 0,
    val totalTaken: Int = 0,
    val totalMissed: Int = 0,
    val currentStreakDays: Int = 0,
    val dailyPoints: List<DailyAdherencePoint> = emptyList()
)

// ----------------------------------------------------
// Side Effects Models
// ----------------------------------------------------

data class DailySeverityPoint(
    val timestamp: Long,
    val dayLabel: String,
    val avgSeverity: Double,
    val count: Int
)

data class SideEffectCategoryCount(
    val categoryName: String,
    val count: Int,
    val percentage: Double,
    val color: Color
)

data class SideEffectItemUiModel(
    val id: String,
    val date: Long,
    val category: String,
    val severity: Int,
    val notes: String?
)

data class SideEffectSummaryUiModel(
    val totalEvents: Int = 0,
    val mostCommon: String? = null,
    val avgSeverity: Double = 0.0,
    val dailySeverityPoints: List<DailySeverityPoint> = emptyList(),
    val categoryCounts: List<SideEffectCategoryCount> = emptyList(),
    val recentSymptoms: List<SideEffectItemUiModel> = emptyList()
)

// ----------------------------------------------------
// Biomarkers Models
// ----------------------------------------------------

data class BiomarkerPoint(
    val timestamp: Long,
    val dayLabel: String,
    val value: Double
)

data class BiomarkerSeriesUiModel(
    val biomarkerName: String,
    val unit: String,
    val baselineValue: Double,
    val currentValue: Double,
    val deltaValue: Double,
    val deltaPercentage: Double,
    val points: List<BiomarkerPoint> = emptyList(),
    val referenceLow: Double? = null,
    val referenceHigh: Double? = null
)

data class BiomarkerDeltaSummary(
    val name: String,
    val unit: String,
    val startValue: Double,
    val currentValue: Double,
    val delta: Double,
    val deltaPercent: Double
)

data class BiomarkerReportsUiModel(
    val availableBiomarkers: List<String> = emptyList(),
    val selectedBiomarker: String = "",
    val activeSeries: BiomarkerSeriesUiModel? = null,
    val allBiomarkerDeltas: List<BiomarkerDeltaSummary> = emptyList()
)

// ----------------------------------------------------
// Subjective Wellness Models
// ----------------------------------------------------

data class DailyWellnessPoint(
    val timestamp: Long,
    val dayLabel: String,
    val mood: Double?,
    val energy: Double?,
    val sleep: Double?,
    val pain: Double?,
    val libido: Double?
)

data class WellnessMetricScore(
    val name: String,
    val currentScore: Double,
    val delta: Double,
    val color: Color
)

data class WellnessTrendUiModel(
    val moodScore: Double = 0.0,
    val energyScore: Double = 0.0,
    val sleepScore: Double = 0.0,
    val painScore: Double = 0.0,
    val libidoScore: Double = 0.0,
    val moodDelta: Double = 0.0,
    val energyDelta: Double = 0.0,
    val sleepDelta: Double = 0.0,
    val painDelta: Double = 0.0,
    val libidoDelta: Double = 0.0,
    val metricsList: List<WellnessMetricScore> = emptyList(),
    val dailyTrends: List<DailyWellnessPoint> = emptyList()
)

// ----------------------------------------------------
// Protocol Comparison Models
// ----------------------------------------------------

data class ProtocolComparisonCardModel(
    val protocolId: String,
    val protocolName: String,
    val goal: String?,
    val isActive: Boolean,
    val totalDays: Int,
    val adherenceRate: Double,
    val totalDosesTaken: Int,
    val totalDosesScheduled: Int,
    val sideEffectCount: Int,
    val avgSideEffectSeverity: Double?,
    val wellnessDelta: Double,
    val topBiomarkerShift: Pair<String, Double>?,
    val highlights: List<String> = emptyList()
)

data class ProtocolComparisonUiModel(
    val protocols: List<ProtocolComparisonCardModel> = emptyList(),
    val bestAdherenceProtocol: String? = null,
    val lowestSideEffectProtocol: String? = null,
    val highestWellnessProtocol: String? = null
)

// ----------------------------------------------------
// Full Screen UI State
// ----------------------------------------------------

data class ReportsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val allProtocols: List<Protocol> = emptyList(),
    val selectedProtocolId: String? = null, // null = All Protocols aggregate
    val selectedTab: ReportsTab = ReportsTab.ADHERENCE,
    val selectedTimeRange: TimeRangeFilter = TimeRangeFilter.DAYS_30,
    val adherenceData: AdherenceSummaryUiModel = AdherenceSummaryUiModel(),
    val sideEffectsData: SideEffectSummaryUiModel = SideEffectSummaryUiModel(),
    val biomarkersData: BiomarkerReportsUiModel = BiomarkerReportsUiModel(),
    val wellnessData: WellnessTrendUiModel = WellnessTrendUiModel(),
    val comparisonData: ProtocolComparisonUiModel = ProtocolComparisonUiModel(),
    val hasAnyData: Boolean = false
)
