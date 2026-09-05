package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.TitrationStep
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

    fun addCustomPeptide(
        nameInput: String,
        categoryInput: String,
        onCreated: (Peptide) -> Unit,
    ) {
        if (_uiState.value.isCreatingCustomPeptide) return
        val name = nameInput.trim()
        val category = categoryInput.trim().ifBlank { "Custom" }
        when {
            name.isBlank() || name.length > 80 -> {
                _uiState.update { it.copy(errorMessage = "Enter a compound name between 1 and 80 characters") }
                return
            }
            category.length > 40 -> {
                _uiState.update { it.copy(errorMessage = "Category must be 40 characters or fewer") }
                return
            }
        }

        val peptide = Peptide(
            id = "custom-${UUID.randomUUID()}",
            name = name,
            category = category,
            description = "User-created compound. No validated reference data is available.",
            halfLifeHours = null,
            halfLifeDisplay = null,
            adminRoute = null,
            typicalFrequency = null,
            typicalDoseRange = null,
            storageInfo = null,
            tags = listOf("custom"),
        )

        _uiState.update { it.copy(isCreatingCustomPeptide = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { peptideRepository.addCustomPeptide(peptide) }
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            peptideId = peptide.id,
                            isCreatingCustomPeptide = false,
                        )
                    }
                    onCreated(peptide)
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isCreatingCustomPeptide = false,
                            errorMessage = error.message ?: "Could not add the custom compound",
                        )
                    }
                }
        }
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

    fun updateTimeOfDay(timeOfDay: String) {
        _uiState.update { it.copy(timeOfDay = timeOfDay) }
    }

    fun setTitrationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(titrationEnabled = enabled) }
    }

    fun updateTitrationWeek(index: Int, value: String) {
        _uiState.update { state ->
            state.copy(titrationSteps = state.titrationSteps.updateAt(index) {
                it.copy(week = value)
            })
        }
    }

    fun updateTitrationDose(index: Int, value: String) {
        _uiState.update { state ->
            state.copy(titrationSteps = state.titrationSteps.updateAt(index) {
                it.copy(doseAmount = value)
            })
        }
    }

    fun addTitrationStep() {
        _uiState.update { state ->
            val nextWeek = (state.titrationSteps.mapNotNull { it.week.toIntOrNull() }.maxOrNull() ?: 0) + 1
            if (nextWeek > MAX_TITRATION_WEEKS) state
            else state.copy(titrationSteps = state.titrationSteps + TitrationDraft(nextWeek.toString(), ""))
        }
    }

    fun removeTitrationStep(index: Int) {
        _uiState.update { state ->
            if (index !in state.titrationSteps.indices || state.titrationSteps.size == 1) state
            else state.copy(titrationSteps = state.titrationSteps.toMutableList().apply { removeAt(index) })
        }
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
        val titrationSteps = state.titrationSteps.mapNotNull { draft ->
            val week = draft.week.toIntOrNull()
            val doseAmount = draft.doseAmount.toDoubleOrNull()
            if (week != null && doseAmount != null && doseAmount.isFinite() && doseAmount > 0.0) {
                TitrationStep(week = week, doseAmount = doseAmount)
            } else {
                null
            }
        }

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
            state.timeOfDay.toLocalTimeOrNull() == null -> {
                _uiState.update { it.copy(errorMessage = "Choose a valid dose time") }
                return
            }
            state.titrationEnabled && titrationSteps.size != state.titrationSteps.size -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid dose for every titration week") }
                return
            }
            state.titrationEnabled && titrationSteps.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Add at least one titration week") }
                return
            }
            state.titrationEnabled && (
                titrationSteps.any { it.week !in 1..MAX_TITRATION_WEEKS } ||
                    titrationSteps.map { it.week }.distinct().size != titrationSteps.size ||
                    titrationSteps.none { it.week == 1 } ||
                    titrationSteps.any { it.doseAmount > MAX_DOSE_AMOUNT }
                ) -> {
                _uiState.update { it.copy(errorMessage = "Titration weeks must be unique, start at week 1, stay within 1-$MAX_TITRATION_WEEKS, and use valid doses") }
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
            timeOfDay = state.timeOfDay,
            adminRoute = route,
            titrationEnabled = state.titrationEnabled,
            titrationSchedule = titrationSteps.sortedBy { it.week }.takeIf { state.titrationEnabled },
            startDate = now,
            endDate = null,
            isActive = true,
            notes = null
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Insert the compound and its generated rows atomically so a
                // failed schedule write cannot leave an apparently configured
                // compound that never appears in Daily Logs.
                protocolRepository.insertCompoundWithDoseSchedule(
                    compound,
                    DoseScheduleGenerator.generate(compound, now)
                )
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

    private fun String.toLocalTimeOrNull() = runCatching {
        java.time.LocalTime.parse(trim())
    }.getOrNull()

    private fun <T> List<T>.updateAt(index: Int, transform: (T) -> T): List<T> =
        if (index !in indices) this else toMutableList().apply { set(index, transform(this[index])) }

    companion object {
        private const val MAX_DOSE_AMOUNT = 1_000_000.0
        private const val MAX_CYCLE_DAYS = 365
        private const val MAX_TITRATION_WEEKS = 52
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
    val timeOfDay: String = "08:00",
    val titrationEnabled: Boolean = false,
    val titrationSteps: List<TitrationDraft> = listOf(TitrationDraft(week = "1", doseAmount = "")),
    val isCreatingCustomPeptide: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class TitrationDraft(
    val week: String,
    val doseAmount: String
)
