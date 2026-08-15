package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCompoundViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val protocolId: String = checkNotNull(savedStateHandle["protocolId"])

    private val _uiState = MutableStateFlow(AddCompoundUiState())
    val uiState: StateFlow<AddCompoundUiState> = _uiState.asStateFlow()

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
    
    fun updateAdminRoute(route: String) {
        _uiState.update { it.copy(adminRoute = route) }
    }

    fun saveCompound(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        val dose = state.doseAmount.toDoubleOrNull()
        if (state.peptideId.isBlank() || dose == null) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid peptide and dose amount") }
            return
        }

        val compound = ProtocolCompound(
            id = UUID.randomUUID().toString(),
            protocolId = protocolId,
            peptideId = state.peptideId, // Ideally this would be selected from encyclopedia
            doseAmount = dose,
            doseUnit = com.appvexis.peptidetracker.core.model.DoseUnit.valueOf(state.doseUnit.uppercase()),
            frequencyType = com.appvexis.peptidetracker.core.model.FrequencyType.valueOf(state.frequencyType.uppercase()),
            frequencyDays = null,
            timeOfDay = "Morning", // Hardcoded fallback for now
            adminRoute = com.appvexis.peptidetracker.core.model.AdminRoute.valueOf(
                state.adminRoute.uppercase().replace("SUBQ", "SUBQ").replace("SUB CUTANEOUS", "SUBQ")
            ),
            titrationEnabled = false,
            titrationSchedule = null,
            startDate = System.currentTimeMillis(),
            endDate = null,
            isActive = true,
            notes = null
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                protocolRepository.insertCompound(compound)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
