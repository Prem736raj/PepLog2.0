package com.appvexis.peptidetracker.feature.protocol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolWithCompounds
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProtocolListViewModel @Inject constructor(
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    val uiState: StateFlow<ProtocolListUiState> = protocolRepository.getAllProtocols()
        .map { protocols ->
            ProtocolListUiState.Success(protocols)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProtocolListUiState.Loading
        )
}

sealed interface ProtocolListUiState {
    object Loading : ProtocolListUiState
    data class Success(val protocols: List<Protocol>) : ProtocolListUiState
    data class Error(val message: String) : ProtocolListUiState
}
