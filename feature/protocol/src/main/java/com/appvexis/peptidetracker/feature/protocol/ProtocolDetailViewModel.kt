package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProtocolDetailViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository,
    peptideRepository: PeptideRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val protocolId: String = checkNotNull(savedStateHandle["protocolId"])

    val uiState: StateFlow<ProtocolDetailUiState> = combine(
        protocolRepository.getProtocolById(protocolId),
        peptideRepository.getAllPeptides()
    ) { protocolWithCompounds, peptides ->
            if (protocolWithCompounds == null) {
                ProtocolDetailUiState.Error("Protocol not found")
            } else {
                ProtocolDetailUiState.Success(
                    protocolWithCompounds = protocolWithCompounds,
                    peptideNames = peptides.associate { it.id to it.name }
                )
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
    data class Success(
        val protocolWithCompounds: ProtocolWithCompounds,
        val peptideNames: Map<String, String>
    ) : ProtocolDetailUiState
    data class Error(val message: String) : ProtocolDetailUiState
}
