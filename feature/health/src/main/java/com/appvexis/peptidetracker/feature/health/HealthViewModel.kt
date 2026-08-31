package com.appvexis.peptidetracker.feature.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.HealthMetricType
import com.appvexis.peptidetracker.core.model.repository.HealthRepository
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import com.appvexis.peptidetracker.feature.health.data.HealthConnectManager
import com.appvexis.peptidetracker.feature.health.data.HealthConnectSyncEngine
import com.appvexis.peptidetracker.feature.health.data.HealthPatternDetector
import com.appvexis.peptidetracker.feature.health.model.CorrelationDataPoint
import com.appvexis.peptidetracker.feature.health.model.HealthConnectStatus
import com.appvexis.peptidetracker.feature.health.model.HealthMetricSummary
import com.appvexis.peptidetracker.feature.health.model.HealthScreenUiState
import com.appvexis.peptidetracker.feature.health.model.HealthTimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val healthRepository: HealthRepository,
    private val logRepository: LogRepository,
    private val protocolRepository: ProtocolRepository,
    private val syncEngine: HealthConnectSyncEngine,
    private val patternDetector: HealthPatternDetector
) : ViewModel() {

    /** Single source of truth for the permission request shown by the screen. */
    val requiredHealthPermissions: Set<String> = healthConnectManager.requiredPermissions

    private val _uiState = MutableStateFlow(HealthScreenUiState())
    val uiState: StateFlow<HealthScreenUiState> = _uiState.asStateFlow()

    init {
        checkHealthConnectStatus()
    }

    /**
     * Check Health Connect availability and permission status.
     */
    private fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val status = when {
                healthConnectManager.isAvailable() -> HealthConnectStatus.AVAILABLE
                healthConnectManager.needsInstall() -> HealthConnectStatus.NOT_INSTALLED
                else -> HealthConnectStatus.NOT_SUPPORTED
            }

            val hasPermissions = if (status == HealthConnectStatus.AVAILABLE) {
                healthConnectManager.hasAllPermissions()
            } else {
                false
            }

            _uiState.update {
                it.copy(
                    healthConnectStatus = status,
                    permissionsGranted = hasPermissions
                )
            }

            if (hasPermissions) {
                loadHealthData()
            }
        }
    }

    /**
     * Called after permissions are granted by the Activity result callback.
     */
    fun onPermissionsGranted() {
        // The result may contain only a subset of requested permissions.
        // Re-read Health Connect instead of treating any non-empty result as success.
        refreshPermissions()
    }

    /**
     * Refresh permissions status (e.g., after returning from settings).
     */
    fun refreshPermissions() {
        viewModelScope.launch {
            val hasPermissions = healthConnectManager.hasAllPermissions()
            _uiState.update { it.copy(permissionsGranted = hasPermissions) }
            if (hasPermissions) {
                loadHealthData()
            }
        }
    }

    /**
     * Trigger a manual sync from Health Connect.
     */
    fun syncAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                syncEngine.syncAll()
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
                loadHealthData()
            } catch (e: Exception) {
                Timber.e(e, "Sync failed")
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Select a metric type to display on the chart.
     */
    fun selectMetricType(type: HealthMetricType) {
        _uiState.update { it.copy(selectedMetricType = type) }
        loadChartData()
    }

    /**
     * Select a time range for the chart.
     */
    fun selectTimeRange(range: HealthTimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        loadChartData()
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Load all health data and compute summaries and patterns.
     */
    private fun loadHealthData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

                // Load health metrics from Room
                val allMetrics = healthRepository.getHealthMetricsInRange(
                    thirtyDaysAgo, now
                ).first()

                // Build metric summaries
                val summaries = HealthMetricType.entries.mapNotNull { type ->
                    val metricsOfType = allMetrics.filter { it.metricType == type }
                    if (metricsOfType.isEmpty()) return@mapNotNull null

                    val sorted = metricsOfType.sortedByDescending { it.timestamp }
                    val latest = sorted.first()
                    val previous = sorted.getOrNull(1)
                    val changePercent = if (previous != null && previous.value > 0) {
                        ((latest.value - previous.value) / previous.value) * 100
                    } else null

                    HealthMetricSummary(
                        metricType = type,
                        latestValue = latest.value,
                        previousValue = previous?.value,
                        changePercent = changePercent,
                        dataPointCount = metricsOfType.size
                    )
                }

                // Load dose logs for correlation
                val doseLogs = logRepository.getDoseLogs(thirtyDaysAgo, now).first()

                // Detect patterns
                val activeProtocols = protocolRepository.getActiveProtocols().first()
                val protocolName = activeProtocols.firstOrNull()?.protocol?.name

                val patterns = patternDetector.detectPatterns(
                    healthMetrics = allMetrics,
                    doseLogs = doseLogs,
                    protocolName = protocolName
                )

                _uiState.update {
                    it.copy(
                        metricSummaries = summaries,
                        allMetrics = allMetrics,
                        doseLogs = doseLogs,
                        patterns = patterns,
                        error = null
                    )
                }

                // Load chart data for selected metric
                loadChartData()

            } catch (e: Exception) {
                Timber.e(e, "Failed to load health data")
                _uiState.update { it.copy(error = "Failed to load data: ${e.message}") }
            }
        }
    }

    /**
     * Load chart data for the currently selected metric type and time range.
     */
    private fun loadChartData() {
        viewModelScope.launch {
            val state = _uiState.value
            val now = System.currentTimeMillis()
            val rangeStart = now - (state.selectedTimeRange.days.toLong() * 24 * 60 * 60 * 1000)

            try {
                val metrics = healthRepository.getHealthMetricsByTypeInRange(
                    state.selectedMetricType, rangeStart, now
                ).first()

                val doseLogs = logRepository.getDoseLogs(rangeStart, now).first()
                val doseTimestamps = doseLogs
                    .filter { it.status == DoseStatus.TAKEN }
                    .mapNotNull { it.actualTime ?: it.scheduledTime }
                    .toSet()

                // Create correlation data points
                val zoneId = java.time.ZoneId.systemDefault()
                val doseDays = doseTimestamps.mapNotNull { timestamp ->
                    runCatching {
                        java.time.Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
                    }.getOrNull()
                }.toSet()
                val chartData = metrics.mapNotNull { metric ->
                    val metricDay = runCatching {
                        java.time.Instant.ofEpochMilli(metric.timestamp).atZone(zoneId).toLocalDate()
                    }.getOrNull() ?: return@mapNotNull null
                    val hasDose = metricDay in doseDays
                    CorrelationDataPoint(
                        timestamp = metric.timestamp,
                        healthValue = metric.value,
                        hasDose = hasDose
                    )
                }

                _uiState.update {
                    it.copy(
                        chartData = chartData,
                        doseMarkers = doseTimestamps.toList()
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load chart data")
            }
        }
    }
}
