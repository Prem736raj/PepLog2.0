package com.appvexis.peptidetracker.feature.encyclopedia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.PeptideDetailRoute
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeptideDetailUiState(
    val peptide: Peptide? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PeptideDetailViewModel @Inject constructor(
    private val peptideRepository: PeptideRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route: PeptideDetailRoute = savedStateHandle.toRoute()
    val peptideId: String = route.peptideId

    val uiState: StateFlow<PeptideDetailUiState> = peptideRepository.getPeptideById(peptideId)
        .map { peptide ->
            PeptideDetailUiState(peptide = peptide, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PeptideDetailUiState(isLoading = true)
        )

    fun toggleBookmark(isCurrentlyBookmarked: Boolean) {
        viewModelScope.launch {
            peptideRepository.setBookmarked(peptideId, !isCurrentlyBookmarked)
        }
    }
}
