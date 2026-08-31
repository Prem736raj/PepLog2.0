package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCompoundViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val peptideRepository: PeptideRepository,
    private val logRepository: LogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val protocolId: String = checkNotNull(savedStateHandle["protocolId"])

    private val _uiState = MutableStateFlow(AddCompoundUiState())
    val uiState: StateFlow<AddCompoundUiState> = _uiState.asStateFlow()

    val peptides: StateFlow<List<Peptide>> = peptideRepository.getAllPeptides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updatePeptideId(id: String) {
        _uiState.update { it.copy(peptideId = id) }
    }

    fun updateDoseAmount(amount: String) {
        _uiState.update { it.copy(doseAmount = amount) }
    }

    fun updateDoseUnit(unit: String) {
        _uiState.update { it.copy(doseUnit = unit) }
    }

    fun updateFrequency(frequency: String) {
        _uiState.update { it.copy(frequencyType = frequency) }
    }

    fun toggleCustomDay(day: Int) {
        if (day !in 1..7) return
        _uiState.update { state ->
            val days = state.customFrequencyDays.toMutableSet()
            if (!days.add(day)) days.remove(day)
            state.copy(customFrequencyDays = days)
        }
    }

    fun updateCycleOnDays(value: String) {
        _uiState.update { it.copy(cycleOnDays = value) }
    }

    fun updateCycleOffDays(value: String) {
        _uiState.update { it.copy(cycleOffDays = value) }
    }

    fun updateAdminRoute(route: String) {
        _uiState.update { it.copy(adminRoute = route) }
    }

    fun saveCompound(onSuccess: () -> Unit) {
        if (_uiState.value.isSaving) return

        val state = _uiState.value
        val dose = state.doseAmount.toDoubleOrNull()
        val selectedPeptide = peptides.value.firstOrNull { it.id == state.peptideId }
        val unit = state.doseUnit.toDoseUnitOrNull()
        val frequency = state.frequencyType.toFrequencyTypeOrNull()
        val route = state.adminRoute.toAdminRouteOrNull()
        val cycleOnDays = state.cycleOnDays.toIntOrNull()
        val cycleOffDays = state.cycleOffDays.toIntOrNull()

        when {
            selectedPeptide == null -> {
                _uiState.update { it.copy(errorMessage = "Select a peptide from the encyclopedia") }
                return
            }
            dose == null || !dose.isFinite() || dose <= 0.0 || dose > MAX_DOSE_AMOUNT -> {
                _uiState.update { it.copy(errorMessage = "Enter a dose between 0 and $MAX_DOSE_AMOUNT") }
                return
            }
            unit == null -> {
                _uiState.update { it.copy(errorMessage = "Select a valid dose unit") }
                return
            }
            frequency == null -> {
                _uiState.update { it.copy(errorMessage = "Select a valid frequency") }
                return
            }
            route == null -> {
                _uiState.update { it.copy(errorMessage = "Select a valid administration route") }
                return
            }
            frequency == FrequencyType.CUSTOM && state.customFrequencyDays.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Select at least one custom frequency day") }
                return
            }
            frequency == FrequencyType.CYCLE &&
                (cycleOnDays == null || cycleOnDays !in 1..MAX_CYCLE_DAYS ||
                    cycleOffDays == null || cycleOffDays !in 0..MAX_CYCLE_DAYS) -> {
                _uiState.update { it.copy(errorMessage = "Cycle days must be on 1-$MAX_CYCLE_DAYS and off 0-$MAX_CYCLE_DAYS") }
                return
            }
        }

        val scheduleDays = when (frequency) {
            FrequencyType.CUSTOM -> state.customFrequencyDays.sorted()
            FrequencyType.CYCLE -> listOf(cycleOnDays!!, cycleOffDays!!)
            else -> null
        }

        val now = System.currentTimeMillis()
        val compound = ProtocolCompound(
            id = UUID.randomUUID().toString(),
            protocolId = protocolId,
            peptideId = selectedPeptide.id,
            doseAmount = dose,
            doseUnit = unit,
            frequencyType = frequency,
            frequencyDays = scheduleDays,
            timeOfDay = DEFAULT_TIME_OF_DAY,
            adminRoute = route,
            titrationEnabled = false,
            titrationSchedule = null,
            startDate = now,
            endDate = null,
            isActive = true,
            notes = null
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                protocolRepository.insertCompound(compound)
                // A compound without dose rows never appears in the daily log.
                // Generate a bounded local schedule immediately after the FK exists.
                logRepository.insertDoseLogs(DoseScheduleGenerator.generate(compound, now))
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Could not add this compound"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun String.toDoseUnitOrNull(): DoseUnit? = runCatching {
        DoseUnit.valueOf(trim().uppercase())
    }.getOrNull()

    private fun String.toFrequencyTypeOrNull(): FrequencyType? = runCatching {
        FrequencyType.valueOf(trim().uppercase())
    }.getOrNull()

    private fun String.toAdminRouteOrNull(): AdminRoute? = when {
        trim().equals("subcutaneous", ignoreCase = true) ||
            trim().equals("subq", ignoreCase = true) -> AdminRoute.SUBQ
        else -> runCatching { AdminRoute.valueOf(trim().uppercase()) }.getOrNull()
    }

    companion object {
        private const val MAX_DOSE_AMOUNT = 1_000_000.0
        private const val MAX_CYCLE_DAYS = 365
        private const val DEFAULT_TIME_OF_DAY = "Morning"
    }
}

data class AddCompoundUiState(
    val peptideId: String = "",
    val doseAmount: String = "",
    val doseUnit: String = DoseUnit.MCG.name,
    val frequencyType: String = FrequencyType.DAILY.name,
    val customFrequencyDays: Set<Int> = emptySet(),
    val cycleOnDays: String = "5",
    val cycleOffDays: String = "2",
    val adminRoute: String = "Subcutaneous",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
