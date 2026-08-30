package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val protocolId: String = requireNotNull(savedStateHandle["protocolId"]) {
        "protocolId is required"
    }

    private val _uiState = MutableStateFlow(AddCompoundUiState())
    val uiState: StateFlow<AddCompoundUiState> = _uiState.asStateFlow()

    fun updatePeptideId(id: String) {
        _uiState.update { it.copy(peptideId = id.take(MAX_TEXT_LENGTH)) }
    }

    fun updateDoseAmount(amount: String) {
        if (amount.length <= MAX_NUMERIC_LENGTH && amount.matches(DECIMAL_REGEX)) {
            _uiState.update { it.copy(doseAmount = amount) }
        }
    }

    fun updateDoseUnit(unit: String) {
        _uiState.update { it.copy(doseUnit = unit.take(MAX_ENUM_TEXT_LENGTH)) }
    }

    fun updateFrequency(frequency: String) {
        _uiState.update { it.copy(frequencyType = frequency.take(MAX_ENUM_TEXT_LENGTH)) }
    }

    fun updateAdminRoute(route: String) {
        _uiState.update { it.copy(adminRoute = route.take(MAX_ENUM_TEXT_LENGTH)) }
    }

    fun saveCompound(onSuccess: () -> Unit) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            val state = _uiState.value
            val dose = state.doseAmount.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it > 0.0 }
            if (dose == null) {
                showError("Enter a positive dose amount")
                return@launch
            }

            val doseUnit = parseDoseUnit(state.doseUnit)
            val frequency = parseFrequency(state.frequencyType)
            val route = parseAdminRoute(state.adminRoute)
            if (doseUnit == null || frequency == null || route == null) {
                showError("Choose a supported dose unit, frequency, and administration route")
                return@launch
            }

            // CUSTOM/CYCLE require schedule details that this screen does not yet collect.
            // Refuse to save an incomplete schedule rather than creating misleading doses.
            if (frequency == FrequencyType.CUSTOM || frequency == FrequencyType.CYCLE) {
                showError("Custom and cycle schedules need day/cycle details before they can be saved")
                return@launch
            }

            val peptideInput = state.peptideId.trim()
            val peptide = peptideRepository.getAllPeptides().first().firstOrNull {
                it.id.equals(peptideInput, ignoreCase = true) ||
                    it.name.equals(peptideInput, ignoreCase = true)
            }
            if (peptide == null) {
                showError("Select a peptide that exists in the encyclopedia")
                return@launch
            }

            val now = System.currentTimeMillis()
            val compound = ProtocolCompound(
                id = UUID.randomUUID().toString(),
                protocolId = protocolId,
                peptideId = peptide.id,
                doseAmount = dose,
                doseUnit = doseUnit,
                frequencyType = frequency,
                frequencyDays = null,
                timeOfDay = "Morning",
                adminRoute = route,
                titrationEnabled = false,
                titrationSchedule = null,
                startDate = now,
                endDate = null,
                isActive = true,
                notes = null
            )

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { protocolRepository.insertCompound(compound) }
                .onSuccess { onSuccess() }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Could not add this compound. Check the details and try again."
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(isSaving = false, errorMessage = message) }
    }

    private fun parseDoseUnit(value: String): DoseUnit? = when (value.trim().lowercase()) {
        "mg" -> DoseUnit.MG
        "mcg", "ug", "µg" -> DoseUnit.MCG
        "iu" -> DoseUnit.IU
        else -> null
    }

    private fun parseFrequency(value: String): FrequencyType? = when (value.trim().lowercase()) {
        "daily", "day" -> FrequencyType.DAILY
        "weekly", "week" -> FrequencyType.WEEKLY
        "custom" -> FrequencyType.CUSTOM
        "cycle", "cycling" -> FrequencyType.CYCLE
        else -> null
    }

    private fun parseAdminRoute(value: String): AdminRoute? = when (
        value.trim().lowercase().replace("-", " ").replace("_", " ")
    ) {
        "subq", "sub q", "subcutaneous", "sub cutaneous" -> AdminRoute.SUBQ
        "im", "intramuscular", "intra muscular" -> AdminRoute.IM
        "intranasal", "intra nasal" -> AdminRoute.INTRANASAL
        "oral" -> AdminRoute.ORAL
        "topical" -> AdminRoute.TOPICAL
        else -> null
    }

    private companion object {
        val DECIMAL_REGEX = Regex("^\\d*\\.?\\d*$")
        const val MAX_TEXT_LENGTH = 80
        const val MAX_ENUM_TEXT_LENGTH = 24
        const val MAX_NUMERIC_LENGTH = 18
    }
}

data class AddCompoundUiState(
    val peptideId: String = "",
    val doseAmount: String = "",
    val doseUnit: String = "mcg",
    val frequencyType: String = "Daily",
    val adminRoute: String = "Subcutaneous",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
