package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProtocolDetailViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val protocolId: String = checkNotNull(savedStateHandle["protocolId"])

    val uiState: StateFlow<ProtocolDetailUiState> = protocolRepository.getProtocolById(protocolId)
        .map { protocolWithCompounds ->
            if (protocolWithCompounds == null) {
                ProtocolDetailUiState.Error("Protocol not found")
            } else {
                ProtocolDetailUiState.Success(protocolWithCompounds)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProtocolDetailUiState.Loading
        )

    fun deleteCompound(compoundId: String) {
        viewModelScope.launch {
            protocolRepository.deleteCompound(compoundId)
        }
    }
}

sealed interface ProtocolDetailUiState {
    object Loading : ProtocolDetailUiState
    data class Success(val protocolWithCompounds: ProtocolWithCompounds) : ProtocolDetailUiState
    data class Error(val message: String) : ProtocolDetailUiState
}
