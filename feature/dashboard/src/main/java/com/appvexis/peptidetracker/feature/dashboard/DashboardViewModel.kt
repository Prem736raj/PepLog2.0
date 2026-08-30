package com.appvexis.peptidetracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.dashboard.model.ActiveProtocolUiModel
import com.appvexis.peptidetracker.feature.dashboard.model.DashboardUiState
import com.appvexis.peptidetracker.feature.dashboard.model.NextDoseInfo
import com.appvexis.peptidetracker.feature.dashboard.model.StreakInfo
import com.appvexis.peptidetracker.feature.dashboard.model.TodayDoseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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

    val uiState: StateFlow<DashboardUiState> = combine(
        logRepository.getDoseLogs(0, Long.MAX_VALUE),
        protocolRepository.getActiveProtocols(),
        peptideRepository.getAllPeptides(),
        inventoryRepository.getAllInventoryItems(),
        _isRefreshing
    ) { allLogs, activeProtocols, peptides, inventoryItems, refreshing ->
        val now = System.currentTimeMillis()
        val (startOfToday, endOfToday) = dayBounds(now)
        val peptideMap = peptides.associateBy { it.id }
        val compoundMap = activeProtocols.flatMap { it.compounds }.associateBy { it.id }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val todaysLogs = allLogs
            .asSequence()
            .filter { it.scheduledTime in startOfToday..endOfToday }
            .sortedBy { it.scheduledTime }
            .toList()

        val todayDosesUi = todaysLogs.map { log ->
            val compound = compoundMap[log.protocolCompoundId]
            val peptide = compound?.let { peptideMap[it.peptideId] }
            val name = peptide?.name ?: compound?.peptideId ?: "Unknown compound"
            val isTaken = log.status == DoseStatus.TAKEN
            val isOverdue = log.status == DoseStatus.PENDING && log.scheduledTime < now

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

        val activeProtocolsUi = activeProtocols.map { pWithC ->
            val p = pWithC.protocol
            val names = pWithC.compounds.map { c ->
                val pep = peptideMap[c.peptideId]
                "${pep?.name ?: c.peptideId} (${c.doseAmount} ${c.doseUnit.name.lowercase()})"
            }

            val start = p.startDate ?: p.createdAt ?: now
            val startDate = toLocalDate(start)
            val today = toLocalDate(now)
            val daysElapsed = maxOf(1, ChronoUnit.DAYS.between(startDate, today).toInt() + 1)
            val totalDays = p.endDate?.let { end ->
                maxOf(1, ChronoUnit.DAYS.between(startDate, toLocalDate(end)).toInt() + 1)
            }
            val progressRatio = totalDays?.let {
                (daysElapsed.toFloat() / it.toFloat()).coerceIn(0f, 1f)
            }

            ActiveProtocolUiModel(
                protocol = p,
                compounds = pWithC.compounds,
                compoundNames = names,
                daysElapsed = daysElapsed,
                totalCycleDays = totalDays,
                progressPercent = progressRatio
            )
        }

        val streakInfo = calculateStreak(allLogs, now)
        val nextDose = findNextDose(allLogs, compoundMap, peptideMap, now, timeFormat)

        val totalVials = inventoryItems
            .filter { it.status != InventoryStatus.EMPTY && it.status != InventoryStatus.EXPIRED }
            .sumOf { it.quantity.coerceAtLeast(0) }
        val expiringVials = inventoryItems.sumOf { item ->
            val expiring = item.isReconstituted && item.expirationDate?.let { expiration ->
                expiration >= now && expiration - now <= TimeUnit.DAYS.toMillis(7)
            } == true && item.status != InventoryStatus.EMPTY && item.status != InventoryStatus.EXPIRED
            if (expiring) item.quantity.coerceAtLeast(0) else 0
        }

        DashboardUiState.Success(
            todayDoses = todayDosesUi,
            totalTodayDoses = totalToday,
            takenTodayDoses = takenToday,
            todayAdherencePercent = adherencePercent,
            activeProtocols = activeProtocolsUi,
            streakInfo = streakInfo,
            nextDose = nextDose,
            totalVialsInStock = totalVials,
            expiringVialsCount = expiringVials,
            isRefreshing = refreshing,
            isEmptyState = activeProtocols.isEmpty() && allLogs.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            runCatching {
                logRepository.logDoseTaken(doseId, System.currentTimeMillis(), null, null)
            }.onFailure { Timber.e(it, "Failed to mark dose as taken") }
        }
    }

    fun quickLogDose(compoundName: String, amount: Double, unit: DoseUnit, notes: String?) {
        if (!amount.isFinite() || amount <= 0.0) return

        viewModelScope.launch {
            val activeProtocols = protocolRepository.getActiveProtocols().first()
            val peptides = peptideRepository.getAllPeptides().first()
            val peptide = peptides.firstOrNull {
                it.id.equals(compoundName.trim(), ignoreCase = true) ||
                    it.name.equals(compoundName.trim(), ignoreCase = true)
            }
            val compound = peptide?.let { matchedPeptide ->
                activeProtocols.asSequence()
                    .flatMap { it.compounds.asSequence() }
                    .firstOrNull { it.peptideId == matchedPeptide.id }
            }

            if (compound == null) {
                Timber.w("Quick log rejected: no active protocol compound matches user selection")
                return@launch
            }

            val now = System.currentTimeMillis()
            runCatching {
                logRepository.insertDoseLog(
                    DoseLog(
                        id = UUID.randomUUID().toString(),
                        protocolCompoundId = compound.id,
                        scheduledTime = now,
                        actualTime = now,
                        doseAmount = amount,
                        doseUnit = unit,
                        status = DoseStatus.TAKEN,
                        injectionSite = null,
                        injectionSide = null,
                        notes = notes?.trim()?.take(MAX_NOTES_LENGTH)?.takeIf { it.isNotBlank() },
                        createdAt = now
                    )
                )
            }.onSuccess {
                hideQuickLogDialog()
            }.onFailure {
                Timber.e(it, "Quick dose log failed")
            }
        }
    }

    fun showQuickLogDialog() {
        _showQuickLogDialog.value = true
    }

    fun hideQuickLogDialog() {
        _showQuickLogDialog.value = false
    }

    /** Reactive flows already refresh the dashboard; this only gives pull-to-refresh
     * a single-frame acknowledgement instead of faking network work for 600 ms. */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            yield()
            _isRefreshing.value = false
        }
    }

    private fun calculateStreak(logs: List<DoseLog>, now: Long): StreakInfo {
        val takenLogs = logs.filter { it.status == DoseStatus.TAKEN && it.actualTime != null }
        if (takenLogs.isEmpty()) return StreakInfo(0, 0, 0, 100)

        val takenDays = takenLogs.mapNotNull { it.actualTime?.let(::toLocalDate) }
            .distinct()
            .sorted()
        val takenDaySet = takenDays.toHashSet()
        val today = toLocalDate(now)
        val streakAnchor = when {
            today in takenDaySet -> today
            today.minusDays(1) in takenDaySet -> today.minusDays(1)
            else -> null
        }

        var currentStreak = 0
        var cursor = streakAnchor
        while (cursor != null && cursor in takenDaySet) {
            currentStreak++
            cursor = cursor.minusDays(1)
        }

        var bestStreak = 0
        var running = 0
        var previous: LocalDate? = null
        for (day in takenDays) {
            running = if (previous != null && previous.plusDays(1) == day) running + 1 else 1
            bestStreak = maxOf(bestStreak, running)
            previous = day
        }

        val totalScheduled = logs.size
        val adherence = if (totalScheduled > 0) {
            (takenLogs.size * 100) / totalScheduled
        } else 100

        return StreakInfo(
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalDosesLogged = takenLogs.size,
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
        val next = allLogs.asSequence()
            .filter { it.status == DoseStatus.PENDING }
            .minByOrNull { it.scheduledTime }
            ?: return null

        val compound = compoundMap[next.protocolCompoundId]
        val peptide = compound?.let { peptideMap[it.peptideId] }
        val name = peptide?.name ?: compound?.peptideId ?: "Unknown compound"
        val diffMs = next.scheduledTime - now
        val isOverdue = diffMs < 0
        val absDiffMs = kotlin.math.abs(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(absDiffMs).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(absDiffMs) % 60).toInt()
        val timeRemaining = when {
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
            timeRemainingDisplay = timeRemaining,
            isOverdue = isOverdue
        )
    }

    private fun dayBounds(timeInMillis: Long): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val date = toLocalDate(timeInMillis)
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }

    private fun toLocalDate(timeInMillis: Long): LocalDate =
        Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()

    private companion object {
        const val MAX_NOTES_LENGTH = 500
    }
}
