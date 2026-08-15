package com.appvexis.peptidetracker.feature.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvexis.peptidetracker.core.model.CalculatorPreset
import com.appvexis.peptidetracker.core.model.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Reconstitution Calculator feature.
 *
 * Handles real-time calculation of concentration, draw volume, and syringe units
 * from user-supplied vial strength, BAC water, and desired dose inputs.
 * Also manages save/load/delete of calculator presets via Room.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    // Input fields
    private val _vialStrengthMg = MutableStateFlow("")
    private val _bacWaterMl = MutableStateFlow("")
    private val _desiredDoseMcg = MutableStateFlow("")
    
    // Unit mode (mg vs mcg for dose input)
    private val _doseUnitIsMcg = MutableStateFlow(true) // Default: mcg
    
    // Syringe type
    private val _syringeType = MutableStateFlow(SyringeType.U100_INSULIN)
    
    // Preset management
    private val _showSaveDialog = MutableStateFlow(false)
    private val _showPresetsSheet = MutableStateFlow(false)
    private val _presetName = MutableStateFlow("")

    // Combine all inputs + presets into a single UiState
    val uiState: StateFlow<CalculatorUiState> = combine(
        _vialStrengthMg,
        _bacWaterMl,
        _desiredDoseMcg,
        _doseUnitIsMcg,
        _syringeType
    ) { vialStr, bacStr, doseStr, isMcg, syringe ->
        val vialMg = vialStr.toDoubleOrNull()
        val bacMl = bacStr.toDoubleOrNull()
        val doseRaw = doseStr.toDoubleOrNull()
        
        // Convert dose to mg for calculation
        val doseMg = if (doseRaw != null) {
            if (isMcg) doseRaw / 1000.0 else doseRaw
        } else null

        val result = if (vialMg != null && bacMl != null && vialMg > 0 && bacMl > 0) {
            val concentration = vialMg / bacMl  // mg/mL
            
            val drawVolume = if (doseMg != null && doseMg > 0 && doseMg <= vialMg) {
                doseMg / concentration  // mL
            } else null
            
            val syringeUnits = if (drawVolume != null) {
                drawVolume * syringe.unitsPerMl  // units
            } else null
            
            val totalDoses = if (doseMg != null && doseMg > 0) {
                (vialMg / doseMg).toInt()
            } else null

            CalculationResult(
                concentrationMgMl = concentration,
                drawVolumeMl = drawVolume,
                syringeUnits = syringeUnits,
                totalDosesPerVial = totalDoses,
                isOverDose = doseMg != null && doseMg > vialMg
            )
        } else null

        CalculatorUiState(
            vialStrengthMg = vialStr,
            bacWaterMl = bacStr,
            desiredDose = doseStr,
            doseUnitIsMcg = isMcg,
            syringeType = syringe,
            result = result
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculatorUiState()
    )

    // Separate flow for presets + dialog state
    val presetsState: StateFlow<PresetsUiState> = combine(
        logRepository.getCalculatorPresets(),
        _showSaveDialog,
        _showPresetsSheet,
        _presetName
    ) { presets, showSave, showSheet, presetName ->
        PresetsUiState(
            presets = presets,
            showSaveDialog = showSave,
            showPresetsSheet = showSheet,
            presetName = presetName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PresetsUiState()
    )

    // --- Input Handlers ---

    fun onVialStrengthChanged(value: String) {
        // Only allow valid decimal numbers
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _vialStrengthMg.value = value
        }
    }

    fun onBacWaterChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _bacWaterMl.value = value
        }
    }

    fun onDesiredDoseChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _desiredDoseMcg.value = value
        }
    }

    fun toggleDoseUnit() {
        _doseUnitIsMcg.value = !_doseUnitIsMcg.value
    }

    fun onSyringeTypeChanged(type: SyringeType) {
        _syringeType.value = type
    }

    fun clearAll() {
        _vialStrengthMg.value = ""
        _bacWaterMl.value = ""
        _desiredDoseMcg.value = ""
    }

    // --- Preset Management ---

    fun showSaveDialog() {
        _presetName.value = ""
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun onPresetNameChanged(name: String) {
        _presetName.value = name
    }

    fun saveCurrentAsPreset() {
        val vialMg = _vialStrengthMg.value.toDoubleOrNull() ?: return
        val bacMl = _bacWaterMl.value.toDoubleOrNull() ?: return
        val doseRaw = _desiredDoseMcg.value.toDoubleOrNull() ?: return
        val doseMg = if (_doseUnitIsMcg.value) doseRaw / 1000.0 else doseRaw
        val name = _presetName.value.ifBlank { "Preset" }

        viewModelScope.launch {
            logRepository.insertCalculatorPreset(
                CalculatorPreset(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    peptideId = null,
                    vialStrengthMg = vialMg,
                    bacWaterMl = bacMl,
                    desiredDoseMg = doseMg,
                    createdAt = System.currentTimeMillis()
                )
            )
            _showSaveDialog.value = false
        }
    }

    fun showPresetsSheet() {
        _showPresetsSheet.value = true
    }

    fun dismissPresetsSheet() {
        _showPresetsSheet.value = false
    }

    fun loadPreset(preset: CalculatorPreset) {
        _vialStrengthMg.value = formatNumber(preset.vialStrengthMg)
        _bacWaterMl.value = formatNumber(preset.bacWaterMl)
        // Always load in mcg
        _doseUnitIsMcg.value = true
        _desiredDoseMcg.value = formatNumber(preset.desiredDoseMg * 1000.0)
        _showPresetsSheet.value = false
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            logRepository.deleteCalculatorPreset(id)
        }
    }

    private fun formatNumber(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}

// --- Data Classes ---

data class CalculatorUiState(
    val vialStrengthMg: String = "",
    val bacWaterMl: String = "",
    val desiredDose: String = "",
    val doseUnitIsMcg: Boolean = true,
    val syringeType: SyringeType = SyringeType.U100_INSULIN,
    val result: CalculationResult? = null
)

data class CalculationResult(
    val concentrationMgMl: Double,
    val drawVolumeMl: Double?,
    val syringeUnits: Double?,
    val totalDosesPerVial: Int?,
    val isOverDose: Boolean = false
)

data class PresetsUiState(
    val presets: List<CalculatorPreset> = emptyList(),
    val showSaveDialog: Boolean = false,
    val showPresetsSheet: Boolean = false,
    val presetName: String = ""
)

enum class SyringeType(val displayName: String, val unitsPerMl: Double) {
    U100_INSULIN("U-100 Insulin (1mL = 100 units)", 100.0),
    U100_HALF("U-100 Half mL (0.5mL = 50 units)", 100.0),
    STANDARD_1ML("Standard 1mL Syringe", 1.0),
    STANDARD_3ML("Standard 3mL Syringe", 1.0)
}
