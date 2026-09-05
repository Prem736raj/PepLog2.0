package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.model.ProtocolStatus
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
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class QuickStartViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    peptideRepository: PeptideRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickStartUiState())
    val uiState: StateFlow<QuickStartUiState> = _uiState.asStateFlow()

    val peptides: StateFlow<List<Peptide>> = peptideRepository.getAllPeptides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updatePeptide(id: String) = _uiState.update { it.copy(peptideId = id) }

    fun updateDoseAmount(value: String) = _uiState.update { it.copy(doseAmount = value) }

    fun updateDoseUnit(value: DoseUnit) = _uiState.update { it.copy(doseUnit = value) }

    fun updateTime(value: String) = _uiState.update { it.copy(timeOfDay = value) }

    fun updateRoute(value: AdminRoute) = _uiState.update { it.copy(adminRoute = value) }

    fun createReminder(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        val peptide = peptides.value.firstOrNull { it.id == state.peptideId }
        val dose = state.doseAmount.toDoubleOrNull()
        val time = runCatching { LocalTime.parse(state.timeOfDay) }.getOrNull()
        when {
            peptide == null -> {
                setError("Choose the compound you want to record")
                return
            }
            dose == null || !dose.isFinite() || dose <= 0.0 || dose > MAX_DOSE_AMOUNT -> {
                setError("Enter a dose between 0 and $MAX_DOSE_AMOUNT")
                return
            }
            time == null -> {
                setError("Choose a valid reminder time")
                return
            }
        }

        val now = System.currentTimeMillis()
        val protocolId = UUID.randomUUID().toString()
        val compoundId = UUID.randomUUID().toString()
        val protocol = Protocol(
            id = protocolId,
            name = peptide.name,
            goal = null,
            status = ProtocolStatus.ACTIVE,
            startDate = now,
            endDate = null,
            notes = null,
            createdAt = now,
            updatedAt = now,
        )
        val compound = ProtocolCompound(
            id = compoundId,
            protocolId = protocolId,
            peptideId = peptide.id,
            doseAmount = dose,
            doseUnit = state.doseUnit,
            frequencyType = FrequencyType.DAILY,
            frequencyDays = null,
            timeOfDay = state.timeOfDay,
            adminRoute = state.adminRoute,
            titrationEnabled = false,
            titrationSchedule = null,
            startDate = now,
            endDate = null,
            isActive = true,
            notes = null,
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                protocolRepository.insertProtocolWithCompoundAndDoseSchedule(
                    protocol = protocol,
                    compound = compound,
                    doseLogs = DoseScheduleGenerator.generate(compound, now),
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Could not create this reminder",
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    companion object {
        private const val MAX_DOSE_AMOUNT = 1_000_000.0
    }
}

data class QuickStartUiState(
    val peptideId: String = "",
    val doseAmount: String = "",
    val doseUnit: DoseUnit = DoseUnit.MCG,
    val timeOfDay: String = "08:00",
    val adminRoute: AdminRoute = AdminRoute.SUBQ,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
