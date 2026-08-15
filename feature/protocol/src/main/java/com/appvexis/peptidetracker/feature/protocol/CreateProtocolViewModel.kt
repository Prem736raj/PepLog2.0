package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolStatus
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
class CreateProtocolViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProtocolUiState())
    val uiState: StateFlow<CreateProtocolUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateGoal(goal: String) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun createProtocol(onSuccess: (String) -> Unit) {
        val currentState = _uiState.value
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Protocol name is required") }
            return
        }

        val protocolId = UUID.randomUUID().toString()
        val protocol = Protocol(
            id = protocolId,
            name = currentState.name,
            goal = currentState.goal.takeIf { it.isNotBlank() },
            status = ProtocolStatus.ACTIVE,
            startDate = System.currentTimeMillis(),
            endDate = null,
            notes = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                protocolRepository.insertProtocol(protocol)
                onSuccess(protocolId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class CreateProtocolUiState(
    val name: String = "",
    val goal: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
