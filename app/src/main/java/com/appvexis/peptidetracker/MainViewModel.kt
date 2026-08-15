package com.appvexis.peptidetracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.datastore.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val isOnboardingCompleted: Boolean) : MainActivityUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = userPreferencesDataSource.isOnboardingCompleted
        .map { completed -> MainActivityUiState.Success(completed) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )
}
