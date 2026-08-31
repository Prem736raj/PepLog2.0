package com.appvexis.peptidetracker.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
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
    private val logRepository: LogRepository,
    protocolRepository: ProtocolRepository,
    peptideRepository: PeptideRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<String?>("All") // "All", "Taken", "Missed"

    val uiState: StateFlow<DoseHistoryUiState> = combine(
        _filterStatus,
        logRepository.getDoseLogs(0, Long.MAX_VALUE),
        protocolRepository.getAllCompounds(),
        peptideRepository.getAllPeptides()
    ) { status, logs, compounds, peptides ->
        val peptideNames = peptides.associateBy { it.id }
        val compoundNames = compounds.associate { compound ->
            compound.id to (peptideNames[compound.peptideId]?.name ?: compound.peptideId)
        }
        val now = System.currentTimeMillis()
        val filtered = when (status) {
            "Taken" -> logs.filter { it.status.name == "TAKEN" }
            "Missed" -> logs.filter {
                it.status.name == "MISSED" ||
                    it.status.name == "SKIPPED" ||
                    (it.status.name == "PENDING" && it.scheduledTime < now)
            }
            else -> logs
        }
        
        // Sort descending by time
        DoseHistoryUiState.Success(
            logs = filtered.sortedByDescending { it.actualTime ?: it.scheduledTime },
            compoundNames = compoundNames
        )
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
    data class Success(
        val logs: List<DoseLog>,
        val compoundNames: Map<String, String>
    ) : DoseHistoryUiState
    data class Error(val message: String) : DoseHistoryUiState
}
