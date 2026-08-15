package com.appvexis.peptidetracker.feature.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.CalculatorPreset
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.JetBrainsMonoFontFamily
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val presetsState by viewModel.presetsState.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reconstitution Calculator",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PepLogTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PepLogTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showPresetsSheet() }) {
                        Icon(
                            imageVector = if (presetsState.presets.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Saved Presets",
                            tint = PepLogTheme.colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background,
                    titleContentColor = PepLogTheme.colors.textPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(PepLogTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
            ) {
                // Input Section
                InputSection(
                    uiState = uiState,
                    onVialStrengthChanged = viewModel::onVialStrengthChanged,
                    onBacWaterChanged = viewModel::onBacWaterChanged,
                    onDesiredDoseChanged = viewModel::onDesiredDoseChanged,
                    onToggleDoseUnit = viewModel::toggleDoseUnit,
                    onClear = viewModel::clearAll
                )

                // Syringe Type Selector
                SyringeTypeSelector(
                    selected = uiState.syringeType,
                    onSelect = viewModel::onSyringeTypeChanged
                )

                // Results Section (animated visibility)
                AnimatedVisibility(
                    visible = uiState.result != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.result?.let { result ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
                        ) {
                            ResultsCard(result = result, syringeType = uiState.syringeType)

                            // Syringe Visualization (only for U-100 insulin syringes)
                            if (uiState.syringeType == SyringeType.U100_INSULIN || uiState.syringeType == SyringeType.U100_HALF) {
                                result.syringeUnits?.let { units ->
                                    val maxUnits = if (uiState.syringeType == SyringeType.U100_HALF) 50.0 else 100.0
                                    PepLogCard(
                                        isGlassmorphic = false,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(PepLogTheme.spacing.small),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "U-100 Syringe View",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = PepLogTheme.colors.textSecondary
                                            )
                                            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                                            SyringeVisualization(
                                                fillUnits = units,
                                                maxUnits = maxUnits,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            // Save Preset Button
                            PepLogButton(
                                text = "Save as Preset",
                                onClick = viewModel::showSaveDialog,
                                modifier = Modifier.fillMaxWidth(),
                                variant = PepLogButtonVariant.Outlined,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = PepLogTheme.colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Safety Info Card
                SafetyInfoCard()

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
            }
        }
    }

    // Save Preset Dialog
    if (presetsState.showSaveDialog) {
        SavePresetDialog(
            presetName = presetsState.presetName,
            onNameChanged = viewModel::onPresetNameChanged,
            onSave = viewModel::saveCurrentAsPreset,
            onDismiss = viewModel::dismissSaveDialog
        )
    }

    // Presets Bottom Sheet
    if (presetsState.showPresetsSheet) {
        PresetsBottomSheet(
            presets = presetsState.presets,
            onLoadPreset = viewModel::loadPreset,
            onDeletePreset = viewModel::deletePreset,
            onDismiss = viewModel::dismissPresetsSheet
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InputSection(
    uiState: CalculatorUiState,
    onVialStrengthChanged: (String) -> Unit,
    onBacWaterChanged: (String) -> Unit,
    onDesiredDoseChanged: (String) -> Unit,
    onToggleDoseUnit: () -> Unit,
    onClear: () -> Unit
) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Reconstitution Parameters",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear all",
                        tint = PepLogTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Vial Strength Input
            PepLogTextField(
                value = uiState.vialStrengthMg,
                onValueChange = onVialStrengthChanged,
                label = "Vial Strength",
                placeholder = "e.g. 5 or 10",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = PepLogTheme.colors.textSecondary
                    )
                },
                trailingIcon = {
                    Text(
                        text = "mg",
                        style = MaterialTheme.typography.labelLarge,
                        color = PepLogTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = PepLogTheme.spacing.medium)
                    )
                }
            )

            // BAC Water Volume Input
            PepLogTextField(
                value = uiState.bacWaterMl,
                onValueChange = onBacWaterChanged,
                label = "Bacteriostatic Water Volume",
                placeholder = "e.g. 2",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PepLogTheme.colors.textSecondary
                    )
                },
                trailingIcon = {
                    Text(
                        text = "mL",
                        style = MaterialTheme.typography.labelLarge,
                        color = PepLogTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = PepLogTheme.spacing.medium)
                    )
                }
            )

            // Desired Dose Input with unit toggle
            PepLogTextField(
                value = uiState.desiredDose,
                onValueChange = onDesiredDoseChanged,
                label = "Desired Dose per Injection",
                placeholder = "e.g. 250",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = PepLogTheme.colors.textSecondary
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onToggleDoseUnit)
                            .padding(horizontal = PepLogTheme.spacing.small, vertical = PepLogTheme.spacing.extraSmall)
                    ) {
                        Text(
                            text = if (uiState.doseUnitIsMcg) "mcg" else "mg",
                            style = MaterialTheme.typography.labelLarge,
                            color = PepLogTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Toggle unit",
                            tint = PepLogTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )

            // Quick dose examples
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.extraSmall)
            ) {
                Text(
                    text = "Quick:",
                    style = MaterialTheme.typography.labelSmall,
                    color = PepLogTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
                listOf("5mg / 2mL", "10mg / 2mL", "10mg / 3mL", "5mg / 1mL").forEach { preset ->
                    val parts = preset.split(" / ")
                    PepLogChip(
                        text = preset,
                        selected = false,
                        onClick = {
                            val vial = parts[0].replace("mg", "")
                            val water = parts[1].replace("mL", "")
                            onVialStrengthChanged(vial)
                            onBacWaterChanged(water)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SyringeTypeSelector(
    selected: SyringeType,
    onSelect: (SyringeType) -> Unit
) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            Text(
                text = "Syringe Type",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PepLogTheme.colors.textSecondary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
            ) {
                SyringeType.entries.forEach { type ->
                    PepLogChip(
                        text = type.displayName,
                        selected = selected == type,
                        onClick = { onSelect(type) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsCard(
    result: CalculationResult,
    syringeType: SyringeType
) {
    val isInsullinSyringe = syringeType == SyringeType.U100_INSULIN || syringeType == SyringeType.U100_HALF
    
    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Calculation Results",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }

            // Over-dose warning
            if (result.isOverDose) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PepLogTheme.colors.accent.copy(alpha = 0.12f))
                        .padding(PepLogTheme.spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PepLogTheme.colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                    Text(
                        text = "Dose exceeds total vial contents!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PepLogTheme.colors.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Concentration result (always shown)
            ResultRow(
                label = "Concentration",
                value = String.format("%.3f mg/mL", result.concentrationMgMl),
                icon = Icons.Default.Science,
                accentColor = PepLogTheme.colors.primary
            )

            // Draw Volume result
            result.drawVolumeMl?.let { volume ->
                ResultRow(
                    label = "Volume to Draw",
                    value = String.format("%.4f mL", volume),
                    icon = Icons.Default.Vaccines,
                    accentColor = PepLogTheme.colors.secondary
                )
            }

            // Syringe Units result
            result.syringeUnits?.let { units ->
                if (isInsullinSyringe) {
                    ResultRow(
                        label = "Syringe Units",
                        value = String.format("%.1f units", units),
                        icon = Icons.Default.Vaccines,
                        accentColor = PepLogTheme.colors.primary,
                        isHighlighted = true
                    )
                }
            }
            
            // Total doses per vial
            result.totalDosesPerVial?.let { totalDoses ->
                ResultRow(
                    label = "Total Doses Per Vial",
                    value = "$totalDoses doses",
                    icon = Icons.Default.LocalPharmacy,
                    accentColor = PepLogTheme.colors.success
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (isHighlighted) {
                    it
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.08f))
                        .padding(PepLogTheme.spacing.small)
                } else {
                    it.padding(vertical = PepLogTheme.spacing.extraSmall)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PepLogTheme.colors.textSecondary
            )
        }
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (isHighlighted) 18.sp else 15.sp,
            color = if (isHighlighted) accentColor else PepLogTheme.colors.textPrimary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SafetyInfoCard() {
    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = PepLogTheme.colors.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Reconstitution Safety Guide",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }
            
            val steps = listOf(
                "Clean rubber stoppers with alcohol swab; let air dry 30 seconds.",
                "Inject BAC water slowly down the INSIDE WALL of the vial — never directly onto powder.",
                "Gently swirl — NEVER shake (shaking can denature peptide bonds).",
                "Solution must be clear and particle-free; discard if cloudy.",
                "Label vial: peptide name, concentration, date reconstituted.",
                "Store at 2–8°C (refrigerator); use within 28 days.",
                "Never reuse needles; always use sterile technique."
            )
            
            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PepLogTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
            
            // Example calculation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PepLogTheme.colors.primary.copy(alpha = 0.06f))
                    .padding(PepLogTheme.spacing.small),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Example: 10mg vial + 2mL BAC water = 5 mg/mL. For 250mcg (0.25mg): 0.25 ÷ 5 = 0.05 mL = 5 units on U-100 syringe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ========== DIALOGS & SHEETS ==========

@Composable
private fun SavePresetDialog(
    presetName: String,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PepLogTheme.colors.surface,
        title = {
            Text(
                text = "Save Preset",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = PepLogTheme.colors.textPrimary
            )
        },
        text = {
            PepLogTextField(
                value = presetName,
                onValueChange = onNameChanged,
                label = "Preset Name",
                placeholder = "e.g. BPC-157 250mcg"
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(
                    text = "Save",
                    color = PepLogTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = PepLogTheme.colors.textSecondary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PresetsBottomSheet(
    presets: List<CalculatorPreset>,
    onLoadPreset: (CalculatorPreset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PepLogTheme.colors.surface,
        contentColor = PepLogTheme.colors.textPrimary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = PepLogTheme.spacing.small)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PepLogTheme.colors.textSecondary.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PepLogTheme.spacing.medium)
                .padding(bottom = PepLogTheme.spacing.large)
        ) {
            Text(
                text = "Saved Presets",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PepLogTheme.colors.textPrimary,
                modifier = Modifier.padding(bottom = PepLogTheme.spacing.medium)
            )
            
            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                        Text(
                            text = "No saved presets yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    modifier = Modifier.height((presets.size.coerceAtMost(5) * 80).dp)
                ) {
                    items(
                        items = presets,
                        key = { it.id }
                    ) { preset ->
                        PresetItem(
                            preset = preset,
                            onLoad = { onLoadPreset(preset) },
                            onDelete = { onDeletePreset(preset.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetItem(
    preset: CalculatorPreset,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    PepLogCard(
        onClick = onLoad,
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PepLogTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PepLogTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${preset.vialStrengthMg}mg / ${preset.bacWaterMl}mL → ${String.format("%.0f", preset.desiredDoseMg * 1000)}mcg",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = PepLogTheme.colors.textSecondary
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete preset",
                    tint = PepLogTheme.colors.accent.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
