package com.appvexis.peptidetracker.feature.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.model.InjectionSiteArea
import com.appvexis.peptidetracker.core.model.InjectionSiteLog
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.feature.injection.model.ReadinessLevel
import com.appvexis.peptidetracker.feature.injection.model.SiteStatus
import com.appvexis.peptidetracker.feature.injection.rotation.RotationSuggestionEngine
import com.appvexis.peptidetracker.feature.injection.rotation.SuggestionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class InjectionTrackerViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    // UI state controls
    private val _selectedSite = MutableStateFlow<InjectionSiteArea?>(null)
    val selectedSite: StateFlow<InjectionSiteArea?> = _selectedSite

    private val _isShowingFront = MutableStateFlow(true)
    val isShowingFront: StateFlow<Boolean> = _isShowingFront

    private val _showLogDialog = MutableStateFlow(false)
    val showLogDialog: StateFlow<Boolean> = _showLogDialog

    // Load all injection site logs (last 90 days for status calculation)
    private val ninetyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)

    val uiState: StateFlow<InjectionTrackerUiState> = combine(
        logRepository.getInjectionSiteLogs(),
        logRepository.getRecentInjectionSiteLogs(ninetyDaysAgo),
        _selectedSite
    ) { allLogs, recentLogs, selected ->
        val now = System.currentTimeMillis()

        // Build site status map from recent logs
        val siteStatuses = buildSiteStatusMap(recentLogs, now)

        // Get last used side for rotation alternation
        val lastUsedSide = allLogs.firstOrNull()?.bodyArea?.let { area ->
            when {
                area.lowercase().contains("left") -> "left"
                area.lowercase().contains("right") -> "right"
                else -> null
            }
        }

        // Get rotation suggestions
        val suggestions = RotationSuggestionEngine.suggestNextSites(
            siteStatuses = siteStatuses,
            lastUsedSide = lastUsedSide
        )

        // Selected site detail
        val selectedDetail = selected?.let { area ->
            val areaLogs = allLogs.filter { it.bodyArea == area.name }
            SelectedSiteDetail(
                area = area,
                status = siteStatuses[area],
                recentLogs = areaLogs.take(10),
                totalUsageCount = areaLogs.size
            )
        }

        InjectionTrackerUiState.Success(
            siteStatuses = siteStatuses,
            topSuggestions = suggestions.take(3),
            selectedDetail = selectedDetail,
            totalInjections = allLogs.size,
            recentInjections = recentLogs.filter {
                it.timestamp >= now - TimeUnit.DAYS.toMillis(7)
            }.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InjectionTrackerUiState.Loading
    )

    fun selectSite(area: InjectionSiteArea) {
        _selectedSite.update { if (it == area) null else area }
    }

    fun clearSelection() {
        _selectedSite.update { null }
    }

    fun toggleBodyView() {
        _isShowingFront.update { !it }
        _selectedSite.update { null } // Clear selection on view toggle
    }

    fun showLogDialog() {
        _showLogDialog.update { true }
    }

    fun hideLogDialog() {
        _showLogDialog.update { false }
    }

    fun logInjectionSite(
        painLevel: Int,
        healingStatus: HealingStatus,
        reactionNotes: String?,
        doseLogId: String? = null
    ) {
        val site = _selectedSite.value ?: return
        viewModelScope.launch {
            val siteLog = InjectionSiteLog(
                id = UUID.randomUUID().toString(),
                doseLogId = doseLogId,
                bodyArea = site.name,
                painLevel = painLevel,
                reactionNotes = reactionNotes?.takeIf { it.isNotBlank() },
                healingStatus = healingStatus,
                timestamp = System.currentTimeMillis()
            )
            logRepository.insertSiteLog(siteLog)
            _showLogDialog.update { false }
        }
    }

    fun updateHealingStatus(logId: String, newStatus: HealingStatus) {
        viewModelScope.launch {
            // Read one snapshot. Collecting this hot database flow here would keep
            // the coroutine alive forever and re-run the update on every emission.
            val existing = logRepository.getInjectionSiteLogs().first()
                .firstOrNull { it.id == logId }
            if (existing != null) {
                logRepository.updateSiteLog(existing.copy(healingStatus = newStatus))
            }
        }
    }

    /**
     * Builds a map of InjectionSiteArea → SiteStatus from recent logs.
     */
    private fun buildSiteStatusMap(
        recentLogs: List<InjectionSiteLog>,
        now: Long
    ): Map<InjectionSiteArea, SiteStatus> {
        val statusMap = mutableMapOf<InjectionSiteArea, SiteStatus>()

        // Group logs by body area
        val grouped = recentLogs.groupBy { it.bodyArea }

        InjectionSiteArea.entries.forEach { area ->
            val areaLogs = grouped[area.name] ?: emptyList()
            val latestLog = areaLogs.maxByOrNull { it.timestamp }

            val daysSinceLastUse = latestLog?.let {
                TimeUnit.MILLISECONDS.toDays(now - it.timestamp).toInt()
            }

            val latestHealingStatus = latestLog?.healingStatus ?: HealingStatus.OK

            statusMap[area] = SiteStatus(
                area = area,
                lastUsedTimestamp = latestLog?.timestamp,
                usageCount = areaLogs.size,
                healingStatus = latestHealingStatus,
                daysSinceLastUse = daysSinceLastUse
            )
        }

        return statusMap
    }
}

sealed interface InjectionTrackerUiState {
    data object Loading : InjectionTrackerUiState
    data class Success(
        val siteStatuses: Map<InjectionSiteArea, SiteStatus>,
        val topSuggestions: List<SuggestionResult>,
        val selectedDetail: SelectedSiteDetail?,
        val totalInjections: Int,
        val recentInjections: Int
    ) : InjectionTrackerUiState
}

data class SelectedSiteDetail(
    val area: InjectionSiteArea,
    val status: SiteStatus?,
    val recentLogs: List<InjectionSiteLog>,
    val totalUsageCount: Int
)
