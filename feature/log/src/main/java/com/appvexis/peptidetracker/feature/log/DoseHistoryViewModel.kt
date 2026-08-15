package com.appvexis.peptidetracker.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DoseHistoryViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<String?>("All") // "All", "Taken", "Missed"

    val uiState: StateFlow<DoseHistoryUiState> = combine(
        _filterStatus,
        logRepository.getDoseLogs(0, Long.MAX_VALUE)
    ) { status, logs ->
        val filtered = when (status) {
            "Taken" -> logs.filter { it.status.name == "TAKEN" }
            "Missed" -> logs.filter { it.status.name == "MISSED" || it.status.name == "SKIPPED" }
            else -> logs
        }
        
        // Sort descending by time
        DoseHistoryUiState.Success(filtered.sortedByDescending { it.actualTime ?: it.scheduledTime })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DoseHistoryUiState.Loading
    )

    fun updateFilter(status: String) {
        _filterStatus.update { status }
    }
}

sealed interface DoseHistoryUiState {
    object Loading : DoseHistoryUiState
    data class Success(val logs: List<DoseLog>) : DoseHistoryUiState
    data class Error(val message: String) : DoseHistoryUiState
}
