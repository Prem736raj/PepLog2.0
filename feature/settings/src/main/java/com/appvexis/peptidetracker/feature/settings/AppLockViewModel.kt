package com.appvexis.peptidetracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.datastore.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val userPreferences: UserPreferencesDataSource,
) : ViewModel() {
    val isAppLockEnabled: StateFlow<Boolean> = userPreferences.isAppLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAppLockEnabled(enabled)
        }
    }
}
