package com.appvexis.peptidetracker.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.datastore.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingGoal(val id: String, val title: String, val description: String) {
    FAT_LOSS("FAT_LOSS", "Fat Loss / Metabolic", "Weight loss, glucose control"),
    HEALING("HEALING", "Healing & Recovery", "Tissue repair, joint health"),
    MUSCLE("MUSCLE", "Muscle & Strength", "Lean muscle mass, performance"),
    FOCUS("FOCUS", "Cognitive & Focus", "Memory, focus, mental clarity"),
    LONGEVITY("LONGEVITY", "Anti-Aging / Skin", "Cellular repair, GHK-Cu skin health")
}

data class OnboardingUiState(
    val selectedGoals: Set<OnboardingGoal> = emptySet(),
    val isOnboardingCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun toggleGoal(goal: OnboardingGoal) {
        _uiState.update { currentState ->
            val updatedGoals = if (currentState.selectedGoals.contains(goal)) {
                currentState.selectedGoals - goal
            } else {
                currentState.selectedGoals + goal
            }
            currentState.copy(selectedGoals = updatedGoals)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val goalIds = _uiState.value.selectedGoals.map { it.id }.toSet()
            userPreferencesDataSource.setSelectedGoals(goalIds)
            userPreferencesDataSource.setOnboardingCompleted(true)
            _uiState.update { it.copy(isOnboardingCompleted = true) }
        }
    }
}
