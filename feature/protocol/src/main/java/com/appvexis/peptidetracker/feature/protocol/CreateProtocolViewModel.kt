package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolStatus
import com.appvexis.peptidetracker.core.billing.SubscriptionManager
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateProtocolViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    private val subscriptionManager: SubscriptionManager
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
        if (currentState.isSaving) return
        val name = currentState.name.trim()
        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Protocol name is required") }
            return
        }
        if (name.length > MAX_NAME_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Protocol name must be $MAX_NAME_LENGTH characters or fewer") }
            return
        }

        val protocolId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val protocol = Protocol(
            id = protocolId,
            name = name,
            goal = currentState.goal.trim().takeIf { it.isNotBlank() },
            status = ProtocolStatus.ACTIVE,
            startDate = now,
            endDate = null,
            notes = null,
            createdAt = now,
            updatedAt = now
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val existingProtocols = protocolRepository.getAllProtocols().first()
                if (!subscriptionManager.isPremium.value && existingProtocols.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "The free plan includes one protocol. Upgrade to create more."
                        )
                    }
                    return@launch
                }
                protocolRepository.insertProtocol(protocol)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess(protocolId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        private const val MAX_NAME_LENGTH = 100
    }
}

data class CreateProtocolUiState(
    val name: String = "",
    val goal: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
