package com.appvexis.peptidetracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.dashboard.model.ActiveProtocolUiModel
import com.appvexis.peptidetracker.feature.dashboard.model.DashboardUiState
import com.appvexis.peptidetracker.feature.dashboard.model.NextDoseInfo
import com.appvexis.peptidetracker.feature.dashboard.model.LoggableCompoundUiModel
import com.appvexis.peptidetracker.feature.dashboard.model.StreakInfo
import com.appvexis.peptidetracker.feature.dashboard.model.TodayDoseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val protocolRepository: ProtocolRepository,
    private val peptideRepository: PeptideRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _showQuickLogDialog = MutableStateFlow(false)
    val showQuickLogDialog: StateFlow<Boolean> = _showQuickLogDialog

    val allPeptides: StateFlow<List<Peptide>> = peptideRepository.getAllPeptides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dashboardLogStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)
    private val dashboardLogEnd = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)

    val uiState: StateFlow<DashboardUiState> = combine(
        logRepository.getDoseLogs(dashboardLogStart, dashboardLogEnd),
        protocolRepository.getActiveProtocols(),
        peptideRepository.getAllPeptides(),
        inventoryRepository.getAllInventoryItems(),
        _isRefreshing
    ) { allLogs, activeProtocols, peptides, inventoryItems, refreshing ->
        val now = System.currentTimeMillis()
        val startOfToday = getStartOfDay(now)
        val endOfToday = getEndOfDay(now)

        val peptideMap = peptides.associateBy { it.id }

        // Find compound mappings across active protocols
        val compoundMap = activeProtocols
            .flatMap { it.compounds }
            .associateBy { it.id }

        // 1. Today's Doses
        val todaysLogs = allLogs.filter { it.scheduledTime in startOfToday..endOfToday }
            .sortedBy { it.scheduledTime }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val todayDosesUi = todaysLogs.map { log ->
            val compound = compoundMap[log.protocolCompoundId]
            val peptide = compound?.let { peptideMap[it.peptideId] }
            val name = peptide?.name ?: compound?.peptideId ?: log.protocolCompoundId
            val isTaken = log.status == DoseStatus.TAKEN
            val isOverdue = !isTaken && log.scheduledTime < now

            TodayDoseUiModel(
                doseLog = log,
                compoundName = name,
                doseDisplay = "${log.doseAmount} ${log.doseUnit.name.lowercase()}",
                timeDisplay = timeFormat.format(Date(log.scheduledTime)),
                isTaken = isTaken,
                isOverdue = isOverdue
            )
        }

        val totalToday = todayDosesUi.size
        val takenToday = todayDosesUi.count { it.isTaken }
        val adherencePercent = if (totalToday > 0) (takenToday * 100) / totalToday else 100

        // 2. Active Protocols
        val activeProtocolsUi = activeProtocols.map { pWithC ->
            val p = pWithC.protocol
            val names = pWithC.compounds.map { c ->
                val pep = peptideMap[c.peptideId]
                val doseStr = "${c.doseAmount} ${c.doseUnit.name.lowercase()}"
                "${pep?.name ?: c.peptideId} ($doseStr)"
            }

            val start = p.startDate ?: p.createdAt ?: now
            val daysElapsed = maxOf(1, TimeUnit.MILLISECONDS.toDays(now - start).toInt() + 1)
            val totalDays = p.endDate?.let { end ->
                maxOf(1, TimeUnit.MILLISECONDS.toDays(end - start).toInt())
            }
            val progressRatio = if (totalDays != null && totalDays > 0) {
                (daysElapsed.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
            } else null

            ActiveProtocolUiModel(
                protocol = p,
                compounds = pWithC.compounds,
                compoundNames = names,
                daysElapsed = daysElapsed,
                totalCycleDays = totalDays,
                progressPercent = progressRatio
            )
        }

        val availableCompounds = activeProtocols
            .flatMap { it.compounds }
            .filter { it.isActive }
            .distinctBy { it.id }
            .map { compound ->
                LoggableCompoundUiModel(
                    id = compound.id,
                    name = peptideMap[compound.peptideId]?.name ?: compound.peptideId,
                    doseAmount = compound.doseAmount,
                    doseUnit = compound.doseUnit
                )
            }

        // 3. Streak Calculation
        val streakInfo = calculateStreak(allLogs, now)

        // 4. Next Scheduled Dose Countdown
        val nextDose = findNextDose(allLogs, compoundMap, peptideMap, now, timeFormat)

        // 5. Inventory Summary
        val totalVials = inventoryItems.sumOf { it.quantity }
        val expiringVials = inventoryItems.count {
            it.isReconstituted && it.expirationDate != null &&
            (it.expirationDate!! - now) <= TimeUnit.DAYS.toMillis(7) &&
            it.status != InventoryStatus.EMPTY
        }

        val isEmpty = activeProtocols.isEmpty() && allLogs.isEmpty()

        DashboardUiState.Success(
            todayDoses = todayDosesUi,
            totalTodayDoses = totalToday,
            takenTodayDoses = takenToday,
            todayAdherencePercent = adherencePercent,
            activeProtocols = activeProtocolsUi,
            availableCompounds = availableCompounds,
            streakInfo = streakInfo,
            nextDose = nextDose,
            totalVialsInStock = totalVials,
            expiringVialsCount = expiringVials,
            isRefreshing = refreshing,
            isEmptyState = isEmpty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            logRepository.logDoseTaken(doseId, System.currentTimeMillis(), null, null)
        }
    }

    fun quickLogDose(compoundId: String, amount: Double, unit: DoseUnit, notes: String?) {
        viewModelScope.launch {
            if (!amount.isFinite() || amount <= 0.0 || amount > MAX_DOSE_AMOUNT) return@launch
            val compound = (uiState.value as? DashboardUiState.Success)
                ?.availableCompounds
                ?.firstOrNull { it.id == compoundId }
            if (compound == null) return@launch
            val now = System.currentTimeMillis()
            val log = DoseLog(
                id = UUID.randomUUID().toString(),
                protocolCompoundId = compound.id,
                scheduledTime = now,
                actualTime = now,
                doseAmount = amount,
                doseUnit = unit,
                status = DoseStatus.TAKEN,
                injectionSite = null,
                injectionSide = null,
                notes = notes,
                createdAt = now
            )
            logRepository.insertDoseLog(log)
            hideQuickLogDialog()
        }
    }

    fun showQuickLogDialog() {
        _showQuickLogDialog.update { true }
    }

    fun hideQuickLogDialog() {
        _showQuickLogDialog.update { false }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.update { true }
            kotlinx.coroutines.delay(600) // Aesthetic refresh pulse
            _isRefreshing.update { false }
        }
    }

    private fun calculateStreak(logs: List<DoseLog>, now: Long): StreakInfo {
        val takenLogs = logs.filter {
            it.status == DoseStatus.TAKEN &&
                it.actualTime != null &&
                it.actualTime!! <= now
        }
            .sortedByDescending { it.actualTime }

        if (takenLogs.isEmpty()) {
            return StreakInfo(0, 0, 0, 100)
        }

        // Group taken logs by day
        val zoneId = java.time.ZoneId.systemDefault()
        val takenDays = takenLogs.map {
            Instant.ofEpochMilli(it.actualTime ?: it.scheduledTime).atZone(zoneId).toLocalDate()
        }
            .distinct()
            .sorted()

        val today = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()
        val yesterday = today.minusDays(1)

        var currentStreak = 0
        val checkDay = when {
            takenDays.contains(today) -> today
            takenDays.contains(yesterday) -> yesterday
            else -> null
        }

        if (checkDay != null) {
            var expectedDay: LocalDate = checkDay
            for (day in takenDays.asReversed()) {
                if (day == expectedDay) {
                    currentStreak++
                    expectedDay = expectedDay.minusDays(1)
                } else if (day.isBefore(expectedDay)) {
                    break
                }
            }
        }

        var bestStreak = 0
        var runningStreak = 0
        var previousDay: LocalDate? = null
        for (day in takenDays) {
            runningStreak = if (previousDay?.plusDays(1) == day) runningStreak + 1 else 1
            bestStreak = maxOf(bestStreak, runningStreak)
            previousDay = day
        }

        // Total Adherence Calculation
        // Future generated rows are not opportunities yet and must not lower adherence.
        val totalScheduled = logs.count {
            it.status != DoseStatus.SKIPPED && it.scheduledTime <= now
        }
        val totalTaken = takenLogs.size
        val adherence = if (totalScheduled > 0) (totalTaken * 100) / totalScheduled else 100

        return StreakInfo(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalDosesLogged = totalTaken,
            overallAdherencePercent = adherence.coerceIn(0, 100)
        )
    }

    private fun findNextDose(
        allLogs: List<DoseLog>,
        compoundMap: Map<String, ProtocolCompound>,
        peptideMap: Map<String, Peptide>,
        now: Long,
        timeFormat: SimpleDateFormat
    ): NextDoseInfo? {
        val pendingDoses = allLogs.filter { it.status == DoseStatus.PENDING }
            .sortedBy { it.scheduledTime }

        val next = pendingDoses.firstOrNull() ?: return null

        val compound = compoundMap[next.protocolCompoundId]
        val peptide = compound?.let { peptideMap[it.peptideId] }
        val name = peptide?.name ?: compound?.peptideId ?: next.protocolCompoundId

        val diffMs = next.scheduledTime - now
        val isOverdue = diffMs < 0
        val absDiffMs = Math.abs(diffMs)

        val hours = TimeUnit.MILLISECONDS.toHours(absDiffMs).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(absDiffMs) % 60).toInt()

        val timeRemainingStr = when {
            isOverdue && hours == 0 && minutes <= 5 -> "Due now"
            isOverdue && hours == 0 -> "$minutes mins overdue"
            isOverdue -> "${hours}h ${minutes}m overdue"
            hours == 0 && minutes <= 5 -> "Due now"
            hours == 0 -> "in $minutes mins"
            else -> "in ${hours}h ${minutes}m"
        }

        return NextDoseInfo(
            doseLog = next,
            compoundName = name,
            doseDisplay = "${next.doseAmount} ${next.doseUnit.name.lowercase()}",
            scheduledTimeDisplay = timeFormat.format(Date(next.scheduledTime)),
            timeRemainingDisplay = timeRemainingStr,
            isOverdue = isOverdue
        )
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

    private fun getEndOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val MAX_DOSE_AMOUNT = 1_000_000.0
    }
}
