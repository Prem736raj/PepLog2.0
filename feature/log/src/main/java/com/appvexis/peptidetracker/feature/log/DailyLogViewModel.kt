package com.appvexis.peptidetracker.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val protocolRepository: ProtocolRepository,
    private val peptideRepository: PeptideRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val currentDate: StateFlow<Long> = _currentDate

    /** Query only the selected day instead of loading the entire dose history into memory. */
    val uiState: StateFlow<DailyLogUiState> = _currentDate
        .flatMapLatest { date ->
            logRepository.getDoseLogs(date, getEndOfDay(date)).map { logs ->
                DailyLogUiState.Success(logs.sortedBy { it.scheduledTime }) as DailyLogUiState
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyLogUiState.Loading
        )

    fun changeDate(newDateMillis: Long) {
        _currentDate.update { getStartOfDay(newDateMillis) }
    }

    fun markDoseAsTaken(doseId: String) {
        viewModelScope.launch {
            runCatching {
                logRepository.logDoseTaken(doseId, System.currentTimeMillis(), null, null)
            }.onFailure { Timber.e(it, "Failed to mark daily-log dose as taken") }
        }
    }

    /**
     * Records an unscheduled dose only when it can be linked to an existing active
     * protocol compound. DoseLog has a foreign key to protocol_compound; storing a
     * peptide name in that column caused a constraint failure and also made
     * inventory/analytics attribution incorrect.
     */
    fun logManualDose(peptideName: String, doseAmount: String, doseUnit: String, notes: String) {
        viewModelScope.launch {
            val amount = doseAmount.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
                ?: return@launch
            val unit = parseDoseUnit(doseUnit) ?: return@launch
            val input = peptideName.trim()
            if (input.isBlank()) return@launch

            val peptides = peptideRepository.getAllPeptides().first()
            val activeProtocols = protocolRepository.getActiveProtocols().first()
            val peptide = peptides.firstOrNull {
                it.id.equals(input, ignoreCase = true) || it.name.equals(input, ignoreCase = true)
            } ?: return@launch
            val compound = activeProtocols.asSequence()
                .flatMap { it.compounds.asSequence() }
                .firstOrNull { it.peptideId == peptide.id }
                ?: return@launch

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
                        notes = notes.trim().take(MAX_NOTES_LENGTH).takeIf { it.isNotBlank() },
                        createdAt = now
                    )
                )
            }.onFailure { Timber.e(it, "Manual dose log failed") }
        }
    }

    private fun parseDoseUnit(value: String): DoseUnit? = when (value.trim().lowercase()) {
        "mg" -> DoseUnit.MG
        "mcg", "ug", "µg" -> DoseUnit.MCG
        "iu" -> DoseUnit.IU
        else -> null
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timeInMillis).atZone(zone).toLocalDate()
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    private fun getEndOfDay(timeInMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timeInMillis).atZone(zone).toLocalDate()
        return date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    }

    private companion object {
        const val MAX_NOTES_LENGTH = 500
    }
}

sealed interface DailyLogUiState {
    data object Loading : DailyLogUiState
    data class Success(val logs: List<DoseLog>) : DailyLogUiState
    data class Error(val message: String) : DailyLogUiState
}
