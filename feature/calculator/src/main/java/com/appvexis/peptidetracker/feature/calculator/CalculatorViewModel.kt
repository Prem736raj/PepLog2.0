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
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the reconstitution arithmetic tool.
 *
 * All values are supplied by the user. The app performs unit conversion and
 * concentration/draw-volume arithmetic; it does not select or recommend a dose.
 */
@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    private val _vialStrengthMg = MutableStateFlow("")
    private val _bacWaterMl = MutableStateFlow("")
    private val _desiredDoseMcg = MutableStateFlow("")
    private val _doseUnitIsMcg = MutableStateFlow(true)
    private val _syringeType = MutableStateFlow(SyringeType.U100_INSULIN)

    private val _showSaveDialog = MutableStateFlow(false)
    private val _showPresetsSheet = MutableStateFlow(false)
    private val _presetName = MutableStateFlow("")

    val uiState: StateFlow<CalculatorUiState> = combine(
        _vialStrengthMg,
        _bacWaterMl,
        _desiredDoseMcg,
        _doseUnitIsMcg,
        _syringeType
    ) { vialStr, bacStr, doseStr, isMcg, syringe ->
        val vialMg = vialStr.toFiniteDoubleOrNull()
        val bacMl = bacStr.toFiniteDoubleOrNull()
        val doseRaw = doseStr.toFiniteDoubleOrNull()
        val doseMg = doseRaw?.let { if (isMcg) it / 1000.0 else it }

        CalculatorUiState(
            vialStrengthMg = vialStr,
            bacWaterMl = bacStr,
            desiredDose = doseStr,
            doseUnitIsMcg = isMcg,
            syringeType = syringe,
            result = ReconstitutionMath.calculate(vialMg, bacMl, doseMg, syringe)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculatorUiState()
    )

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

    fun onVialStrengthChanged(value: String) {
        if (value.isValidDecimalInput()) _vialStrengthMg.value = value
    }

    fun onBacWaterChanged(value: String) {
        if (value.isValidDecimalInput()) _bacWaterMl.value = value
    }

    fun onDesiredDoseChanged(value: String) {
        if (value.isValidDecimalInput()) _desiredDoseMcg.value = value
    }

    /**
     * Converts the currently entered numeric value when switching units. Merely
     * relabelling 250 mcg as 250 mg would create a 1000x arithmetic error.
     */
    fun toggleDoseUnit() {
        val currentlyMcg = _doseUnitIsMcg.value
        val currentValue = _desiredDoseMcg.value.toFiniteDoubleOrNull()
        if (currentValue != null) {
            val converted = if (currentlyMcg) currentValue / 1000.0 else currentValue * 1000.0
            _desiredDoseMcg.value = formatNumber(converted)
        }
        _doseUnitIsMcg.value = !currentlyMcg
    }

    fun onSyringeTypeChanged(type: SyringeType) {
        _syringeType.value = type
    }

    fun clearAll() {
        _vialStrengthMg.value = ""
        _bacWaterMl.value = ""
        _desiredDoseMcg.value = ""
    }

    fun showSaveDialog() {
        val state = uiState.value
        if (state.result?.isValidForDrawing != true) return
        _presetName.value = ""
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun onPresetNameChanged(name: String) {
        _presetName.value = name.take(MAX_PRESET_NAME_LENGTH)
    }

    fun saveCurrentAsPreset() {
        val vialMg = _vialStrengthMg.value.toFiniteDoubleOrNull() ?: return
        val bacMl = _bacWaterMl.value.toFiniteDoubleOrNull() ?: return
        val doseRaw = _desiredDoseMcg.value.toFiniteDoubleOrNull() ?: return
        val doseMg = if (_doseUnitIsMcg.value) doseRaw / 1000.0 else doseRaw
        val result = ReconstitutionMath.calculate(vialMg, bacMl, doseMg, _syringeType.value)
        if (result?.isValidForDrawing != true) return

        val name = _presetName.value.trim().ifBlank { "Preset" }

        viewModelScope.launch {
            runCatching {
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
            }.onSuccess {
                _showSaveDialog.value = false
            }
        }
    }

    fun showPresetsSheet() {
        _showPresetsSheet.value = true
    }

    fun dismissPresetsSheet() {
        _showPresetsSheet.value = false
    }

    fun loadPreset(preset: CalculatorPreset) {
        if (
            !preset.vialStrengthMg.isPositiveFinite() ||
            !preset.bacWaterMl.isPositiveFinite() ||
            !preset.desiredDoseMg.isPositiveFinite()
        ) return

        _vialStrengthMg.value = formatNumber(preset.vialStrengthMg)
        _bacWaterMl.value = formatNumber(preset.bacWaterMl)
        _doseUnitIsMcg.value = true
        _desiredDoseMcg.value = formatNumber(preset.desiredDoseMg * 1000.0)
        _showPresetsSheet.value = false
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            runCatching { logRepository.deleteCalculatorPreset(id) }
        }
    }

    private fun String.toFiniteDoubleOrNull(): Double? =
        toDoubleOrNull()?.takeIf { it.isFinite() }

    private fun String.isValidDecimalInput(): Boolean =
        length <= MAX_NUMERIC_INPUT_LENGTH &&
            (isEmpty() || matches(DECIMAL_REGEX))

    private fun Double.isPositiveFinite(): Boolean = isFinite() && this > 0.0

    private fun formatNumber(value: Double): String {
        if (!value.isFinite()) return ""
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    }

    private companion object {
        val DECIMAL_REGEX = Regex("^\\d*\\.?\\d*$")
        const val MAX_NUMERIC_INPUT_LENGTH = 18
        const val MAX_PRESET_NAME_LENGTH = 60
    }
}

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
    val drawVolumeMl: Double? = null,
    val syringeUnits: Double? = null,
    val totalDosesPerVial: Long? = null,
    val isOverDose: Boolean = false,
    val isSyringeCapacityExceeded: Boolean = false
) {
    val isValidForDrawing: Boolean
        get() = drawVolumeMl != null && !isOverDose && !isSyringeCapacityExceeded
}

data class PresetsUiState(
    val presets: List<CalculatorPreset> = emptyList(),
    val showSaveDialog: Boolean = false,
    val showPresetsSheet: Boolean = false,
    val presetName: String = ""
)

enum class SyringeType(
    val displayName: String,
    val capacityMl: Double,
    val usesU100Units: Boolean
) {
    U100_INSULIN("U-100 1 mL (100 units)", capacityMl = 1.0, usesU100Units = true),
    U100_HALF("U-100 0.5 mL (50 units)", capacityMl = 0.5, usesU100Units = true),
    STANDARD_1ML("Standard 1 mL Syringe", capacityMl = 1.0, usesU100Units = false),
    STANDARD_3ML("Standard 3 mL Syringe", capacityMl = 3.0, usesU100Units = false)
}
