package com.appvexis.peptidetracker.feature.dashboard.model

import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolCompound

/**
 * UI representation of a scheduled dose for today.
 */
data class TodayDoseUiModel(
    val doseLog: DoseLog,
    val compoundName: String,
    val doseDisplay: String,
    val timeDisplay: String,
    val isTaken: Boolean,
    val isOverdue: Boolean
)

/**
 * UI representation of an active protocol stack on the dashboard.
 */
data class ActiveProtocolUiModel(
    val protocol: Protocol,
    val compounds: List<ProtocolCompound>,
    val compoundNames: List<String>,
    val daysElapsed: Int,
    val totalCycleDays: Int?,
    val progressPercent: Float?
)

/**
 * Adherence streak tracking statistics.
 */
data class StreakInfo(
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val totalDosesLogged: Int,
    val overallAdherencePercent: Int
)

/**
 * Info for the next upcoming dose countdown.
 */
data class NextDoseInfo(
    val doseLog: DoseLog,
    val compoundName: String,
    val doseDisplay: String,
    val scheduledTimeDisplay: String,
    val timeRemainingDisplay: String,
    val isOverdue: Boolean
)

/** A selectable active-protocol compound for quick dose logging. */
data class LoggableCompoundUiModel(
    val id: String,
    val name: String,
    val doseAmount: Double,
    val doseUnit: DoseUnit
)

/**
 * Comprehensive reactive state for the Dashboard.
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val todayDoses: List<TodayDoseUiModel>,
        val totalTodayDoses: Int,
        val takenTodayDoses: Int,
        val todayAdherencePercent: Int,
        val activeProtocols: List<ActiveProtocolUiModel>,
        val availableCompounds: List<LoggableCompoundUiModel>,
        val streakInfo: StreakInfo,
        val nextDose: NextDoseInfo?,
        val totalVialsInStock: Int,
        val expiringVialsCount: Int,
        val isRefreshing: Boolean = false,
        val isEmptyState: Boolean = false
    ) : DashboardUiState
}
