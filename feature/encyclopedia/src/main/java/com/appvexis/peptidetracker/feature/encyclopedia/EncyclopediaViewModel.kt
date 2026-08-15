package com.appvexis.peptidetracker.feature.encyclopedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EncyclopediaUiState(
    val peptides: List<Peptide> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val showBookmarksOnly: Boolean = false,
    val categories: List<String> = listOf(
        "GH Secretagogues",
        "Healing & Recovery",
        "Weight Loss / Metabolic",
        "Mitochondrial / Longevity",
        "Cognitive / Nootropic",
        "Anti-Aging / Skin",
        "Sexual Health",
        "Immune / Thymic"
    ),
    val isLoading: Boolean = false
)

@HiltViewModel
class EncyclopediaViewModel @Inject constructor(
    private val peptideRepository: PeptideRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _showBookmarksOnly = MutableStateFlow(false)
    val showBookmarksOnly = _showBookmarksOnly.asStateFlow()

    val uiState: StateFlow<EncyclopediaUiState> = combine(
        peptideRepository.getAllPeptides(),
        _searchQuery,
        _selectedCategory,
        _showBookmarksOnly
    ) { allPeptides, query, category, bookmarksOnly ->
        val filtered = allPeptides.filter { peptide ->
            val matchesQuery = query.isEmpty() || 
                    peptide.name.contains(query, ignoreCase = true) || 
                    (peptide.description?.contains(query, ignoreCase = true) == true) ||
                    peptide.aliases.any { it.contains(query, ignoreCase = true) }
            
            val matchesCategory = category == null || 
                    peptide.category == category || 
                    peptide.tags.contains(category)
            val matchesBookmark = !bookmarksOnly || peptide.isBookmarked
            
            matchesQuery && matchesCategory && matchesBookmark
        }
        EncyclopediaUiState(
            peptides = filtered,
            searchQuery = query,
            selectedCategory = category,
            showBookmarksOnly = bookmarksOnly,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EncyclopediaUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleBookmarkFilter() {
        _showBookmarksOnly.value = !_showBookmarksOnly.value
    }

    fun toggleBookmark(peptideId: String, isCurrentlyBookmarked: Boolean) {
        viewModelScope.launch {
            peptideRepository.setBookmarked(peptideId, !isCurrentlyBookmarked)
        }
    }
}
