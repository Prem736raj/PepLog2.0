package com.appvexis.peptidetracker.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.LoggableCompound
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val logRepository: LogRepository,
    protocolRepository: ProtocolRepository,
    peptideRepository: PeptideRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val currentDate: StateFlow<Long> = _currentDate.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val availableCompounds: Flow<List<LoggableCompound>> = combine(
        protocolRepository.getActiveProtocols(),
        peptideRepository.getAllPeptides()
    ) { protocols, peptides ->
        val peptideNames = peptides.associateBy { it.id }
        protocols.flatMap { protocol ->
            protocol.compounds.filter { it.isActive }.map { compound ->
                LoggableCompound(
                    id = compound.id,
                    protocolId = compound.protocolId,
                    peptideId = compound.peptideId,
                    name = peptideNames[compound.peptideId]?.name ?: compound.peptideId,
                    doseAmount = compound.doseAmount,
                    doseUnit = compound.doseUnit
                )
            }
        }.distinctBy { it.id }
    }

    private val logsForCurrentDate: Flow<List<DoseLog>> = _currentDate.flatMapLatest { date ->
        logRepository.getDoseLogs(date, getEndOfDay(date))
    }

    val uiState: StateFlow<DailyLogUiState> = combine(
        logsForCurrentDate,
        availableCompounds
    ) { logs, compounds ->
        DailyLogUiState.Success(
            logs = logs.sortedBy { it.scheduledTime },
            availableCompounds = compounds,
            compoundNames = compounds.associate { it.id to it.name }
        ) as DailyLogUiState
    }.catch { error ->
        emit(DailyLogUiState.Error(error.message ?: "Could not load today's doses"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyLogUiState.Loading
    )

    fun changeDate(newDateMillis: Long) {
        _currentDate.update { getStartOfDay(newDateMillis) }
    }

    fun changeDateByDays(days: Int) {
        _currentDate.update {
            Calendar.getInstance().apply {
                timeInMillis = it
                add(Calendar.DAY_OF_MONTH, days)
            }.timeInMillis.let(::getStartOfDay)
        }
    }

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            runCatching {
                logRepository.logDoseTaken(doseId, System.currentTimeMillis(), null, null)
            }.onFailure { error ->
                _actionError.value = error.message ?: "Could not mark the dose as taken"
            }
        }
    }

    fun logManualDose(compoundId: String, doseAmount: String, doseUnit: String, notes: String) {
        viewModelScope.launch {
            val amount = doseAmount.toDoubleOrNull()
            val unit = runCatching { DoseUnit.valueOf(doseUnit.trim().uppercase()) }.getOrNull()
            val compound = (uiState.value as? DailyLogUiState.Success)
                ?.availableCompounds
                ?.firstOrNull { it.id == compoundId }

            when {
                compound == null -> _actionError.value = "Select an active protocol compound"
                amount == null || !amount.isFinite() || amount <= 0.0 || amount > MAX_DOSE_AMOUNT -> {
                    _actionError.value = "Enter a dose between 0 and $MAX_DOSE_AMOUNT"
                }
                unit == null -> _actionError.value = "Select a valid dose unit"
                else -> runCatching {
                    val now = System.currentTimeMillis()
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
                            notes = notes.trim().takeIf { it.isNotEmpty() },
                            createdAt = now
                        )
                    )
                }.onFailure { error ->
                    _actionError.value = error.message ?: "Could not save the dose"
                }
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
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
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MILLISECOND, -1)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val MAX_DOSE_AMOUNT = 1_000_000.0
    }
}

sealed interface DailyLogUiState {
    data object Loading : DailyLogUiState
    data class Success(
        val logs: List<DoseLog>,
        val availableCompounds: List<LoggableCompound>,
        val compoundNames: Map<String, String>
    ) : DailyLogUiState
    data class Error(val message: String) : DailyLogUiState
}
