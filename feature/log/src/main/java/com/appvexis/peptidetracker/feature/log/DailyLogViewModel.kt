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
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val currentDate: StateFlow<Long> = _currentDate

    val uiState: StateFlow<DailyLogUiState> = combine(
        _currentDate,
        logRepository.getDoseLogs(0, Long.MAX_VALUE) // In a real app we'd filter by date range, but we filter in memory here for simplicity
    ) { date, allLogs ->
        val endOfDay = getEndOfDay(date)
        val todaysLogs = allLogs.filter { 
            it.scheduledTime in date..endOfDay 
        }.sortedBy { it.scheduledTime }
        
        DailyLogUiState.Success(todaysLogs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyLogUiState.Loading
    )

    fun changeDate(newDateMillis: Long) {
        _currentDate.update { getStartOfDay(newDateMillis) }
    }

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            logRepository.logDoseTaken(doseId, System.currentTimeMillis(), null, null)
            // Note: Trigger precomputed analytics update here
        }
    }

    fun logManualDose(peptideName: String, doseAmount: String, doseUnit: String, notes: String) {
        viewModelScope.launch {
            val amount = doseAmount.toDoubleOrNull() ?: return@launch
            val unit = try { com.appvexis.peptidetracker.core.model.DoseUnit.valueOf(doseUnit.uppercase()) } catch (e: Exception) { return@launch }
            
            val manualLog = DoseLog(
                id = java.util.UUID.randomUUID().toString(),
                protocolCompoundId = peptideName, // In reality, this links to an active protocol compound, but for manual logs we might use a special compound or just string match
                scheduledTime = System.currentTimeMillis(),
                actualTime = System.currentTimeMillis(),
                doseAmount = amount,
                doseUnit = unit,
                status = com.appvexis.peptidetracker.core.model.DoseStatus.TAKEN,
                injectionSite = null,
                injectionSide = null,
                notes = notes.takeIf { it.isNotBlank() },
                createdAt = System.currentTimeMillis()
            )
            
            logRepository.insertDoseLog(manualLog)
            // Note: Trigger precomputed analytics update here
        }
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
}

sealed interface DailyLogUiState {
    object Loading : DailyLogUiState
    data class Success(val logs: List<DoseLog>) : DailyLogUiState
    data class Error(val message: String) : DailyLogUiState
}
