package com.appvexis.peptidetracker.feature.inventory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.repository.InventoryRepository
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import com.appvexis.peptidetracker.feature.inventory.model.ExpirationStatus
import com.appvexis.peptidetracker.feature.inventory.model.InventoryFilter
import com.appvexis.peptidetracker.feature.inventory.model.VialUiModel
import com.appvexis.peptidetracker.feature.inventory.model.toUiModel
import com.appvexis.peptidetracker.feature.inventory.notification.InventoryNotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val peptideRepository: PeptideRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeFilter = MutableStateFlow(InventoryFilter.ALL)
    val activeFilter: StateFlow<InventoryFilter> = _activeFilter

    private val _selectedVialForDetail = MutableStateFlow<VialUiModel?>(null)
    val selectedVialForDetail: StateFlow<VialUiModel?> = _selectedVialForDetail

    private val _selectedVialForReconstitution = MutableStateFlow<VialUiModel?>(null)
    val selectedVialForReconstitution: StateFlow<VialUiModel?> = _selectedVialForReconstitution

    private val _selectedVialForVolumeAdjust = MutableStateFlow<VialUiModel?>(null)
    val selectedVialForVolumeAdjust: StateFlow<VialUiModel?> = _selectedVialForVolumeAdjust

    private val _vialToEdit = MutableStateFlow<InventoryItem?>(null)
    val vialToEdit: StateFlow<InventoryItem?> = _vialToEdit

    private val _showAddVialDialog = MutableStateFlow(false)
    val showAddVialDialog: StateFlow<Boolean> = _showAddVialDialog

    val allPeptides: StateFlow<List<Peptide>> = peptideRepository.getAllPeptides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allVials: StateFlow<List<VialUiModel>> = combine(
        inventoryRepository.getAllInventoryItems(),
        peptideRepository.getAllPeptides()
    ) { items, peptides ->
        val peptideMap = peptides.associateBy { it.id }
        items.map { item ->
            val peptide = peptideMap[item.peptideId]
            item.toUiModel(
                peptideName = peptide?.name ?: item.peptideId,
                peptideCategory = peptide?.category ?: "Custom Peptide"
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allVials
                .collect { vials ->
                    InventoryNotificationHelper.checkAndNotifyInventoryAlerts(context, vials)
                }
        }
    }

    val uiState: StateFlow<InventoryUiState> = combine(
        allVials,
        _searchQuery,
        _activeFilter
    ) { allVialUiModels, query, filter ->

        // Summary counts
        val totalVials = allVialUiModels.sumOf { it.item.quantity }
        val inUseCount = allVialUiModels.count { it.item.status == InventoryStatus.IN_USE || it.item.isReconstituted }
        val unmixedCount = allVialUiModels.count { !it.item.isReconstituted && it.item.status != InventoryStatus.EMPTY && it.item.status != InventoryStatus.EXPIRED }
        val expiringCount = allVialUiModels.count { it.expirationStatus == ExpirationStatus.CRITICAL || it.expirationStatus == ExpirationStatus.EXPIRING_SOON || it.expirationStatus == ExpirationStatus.EXPIRED }
        val lowVolumeCount = allVialUiModels.count { it.isLowVolume }
        val alertCount = allVialUiModels.count {
            it.expirationStatus == ExpirationStatus.CRITICAL ||
            it.expirationStatus == ExpirationStatus.EXPIRING_SOON ||
            it.expirationStatus == ExpirationStatus.EXPIRED ||
            it.isLowVolume
        }

        // Filtering
        val filtered = allVialUiModels.filter { vial ->
            // Search Query filter (matches peptide name, category, vendor, batch #)
            val matchesQuery = if (query.isBlank()) true else {
                vial.peptideName.contains(query, ignoreCase = true) ||
                vial.peptideCategory.contains(query, ignoreCase = true) ||
                (vial.item.vendor?.contains(query, ignoreCase = true) == true) ||
                (vial.item.batchNumber?.contains(query, ignoreCase = true) == true)
            }

            // Tab filter
            val matchesFilter = when (filter) {
                InventoryFilter.ALL -> true
                InventoryFilter.IN_USE -> vial.item.isReconstituted && vial.item.status == InventoryStatus.IN_USE
                InventoryFilter.IN_STOCK -> !vial.item.isReconstituted && vial.item.status == InventoryStatus.IN_STOCK
                InventoryFilter.EXPIRING_OR_LOW -> vial.expirationStatus == ExpirationStatus.CRITICAL ||
                        vial.expirationStatus == ExpirationStatus.EXPIRING_SOON ||
                        vial.expirationStatus == ExpirationStatus.EXPIRED ||
                        vial.isLowVolume
                InventoryFilter.EXPIRED -> vial.expirationStatus == ExpirationStatus.EXPIRED || vial.item.status == InventoryStatus.EXPIRED
                InventoryFilter.EMPTY -> vial.item.status == InventoryStatus.EMPTY || (vial.item.isReconstituted && (vial.item.remainingVolumeMl ?: 0.0) <= 0.0)
            }

            matchesQuery && matchesFilter
        }

        InventoryUiState.Success(
            vials = filtered,
            totalVialsCount = totalVials,
            inUseCount = inUseCount,
            unmixedCount = unmixedCount,
            expiringCount = expiringCount,
            lowVolumeCount = lowVolumeCount,
            alertCount = alertCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState.Loading
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    fun notifyCurrentAlerts() {
        InventoryNotificationHelper.checkAndNotifyInventoryAlerts(context, allVials.value)
    }

    fun selectFilter(filter: InventoryFilter) {
        _activeFilter.update { filter }
    }

    fun showAddDialog() {
        _vialToEdit.update { null }
        _showAddVialDialog.update { true }
    }

    fun hideAddDialog() {
        _showAddVialDialog.update { false }
        _vialToEdit.update { null }
    }

    fun openEditDialog(item: InventoryItem) {
        _vialToEdit.update { item }
        _showAddVialDialog.update { true }
    }

    fun openReconstitutionDialog(vial: VialUiModel) {
        _selectedVialForReconstitution.update { vial }
    }

    fun hideReconstitutionDialog() {
        _selectedVialForReconstitution.update { null }
    }

    fun openVolumeAdjustDialog(vial: VialUiModel) {
        _selectedVialForVolumeAdjust.update { vial }
    }

    fun hideVolumeAdjustDialog() {
        _selectedVialForVolumeAdjust.update { null }
    }

    fun openDetailDialog(vial: VialUiModel) {
        _selectedVialForDetail.update { vial }
    }

    fun hideDetailDialog() {
        _selectedVialForDetail.update { null }
    }

    fun saveVial(item: InventoryItem) {
        viewModelScope.launch {
            if (_vialToEdit.value != null) {
                inventoryRepository.updateInventoryItem(item)
            } else {
                inventoryRepository.insertInventoryItem(item)
            }
            hideAddDialog()
        }
    }

    fun deleteVial(id: String) {
        viewModelScope.launch {
            inventoryRepository.deleteInventoryItem(id)
            if (_selectedVialForDetail.value?.item?.id == id) {
                hideDetailDialog()
            }
        }
    }

    fun reconstituteVial(id: String, bacWaterMl: Double, date: Long) {
        viewModelScope.launch {
            inventoryRepository.reconstituteVial(id, bacWaterMl, date)
            hideReconstitutionDialog()
        }
    }

    fun adjustVolume(id: String, newVolumeMl: Double) {
        viewModelScope.launch {
            val item = (uiState.value as? InventoryUiState.Success)?.vials?.find { it.item.id == id }?.item
            if (item != null && newVolumeMl.isFinite() && newVolumeMl >= 0.0) {
                val updated = item.copy(
                    remainingVolumeMl = newVolumeMl,
                    status = if (newVolumeMl <= 0.001) InventoryStatus.EMPTY else item.status
                )
                inventoryRepository.updateInventoryItem(updated)
            }
            hideVolumeAdjustDialog()
        }
    }

    fun markVialAsEmpty(id: String) {
        viewModelScope.launch {
            val item = (uiState.value as? InventoryUiState.Success)?.vials?.find { it.item.id == id }?.item
            if (item != null) {
                val updated = item.copy(
                    remainingVolumeMl = 0.0,
                    status = InventoryStatus.EMPTY
                )
                inventoryRepository.updateInventoryItem(updated)
            }
            hideVolumeAdjustDialog()
            hideDetailDialog()
        }
    }
}

sealed interface InventoryUiState {
    data object Loading : InventoryUiState
    data class Success(
        val vials: List<VialUiModel>,
        val totalVialsCount: Int,
        val inUseCount: Int,
        val unmixedCount: Int,
        val expiringCount: Int,
        val lowVolumeCount: Int,
        val alertCount: Int
    ) : InventoryUiState
}
