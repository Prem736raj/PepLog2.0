package com.appvexis.peptidetracker.feature.protocol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.model.FrequencyType
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCompoundScreen(
    protocolId: String,
    onBackClick: () -> Unit,
    onCompoundAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddCompoundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val peptides by viewModel.peptides.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Compound",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background
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
                    .padding(PepLogTheme.spacing.medium)
            ) {
                Text(
                    text = "Choose a catalog peptide, then set the schedule used by your daily log.",
                    color = PepLogTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                var peptideMenuExpanded by remember { mutableStateOf(false) }
                val selectedPeptide = peptides.firstOrNull { it.id == uiState.peptideId }
                SelectionField(
                    label = "Peptide",
                    value = selectedPeptide?.name ?: "Select a peptide",
                    expanded = peptideMenuExpanded,
                    onExpandChange = { peptideMenuExpanded = it }
                ) {
                    peptides.forEach { peptide ->
                        DropdownMenuItem(
                            text = { Text("${peptide.name} · ${peptide.category}") },
                            onClick = {
                                viewModel.updatePeptideId(peptide.id)
                                peptideMenuExpanded = false
                            }
                        )
                    }
                }

                if (peptides.isEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Peptide catalog is still loading. Try again in a moment.",
                        color = PepLogTheme.colors.accent,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                Row(modifier = Modifier.fillMaxWidth()) {
                    PepLogTextField(
                        value = uiState.doseAmount,
                        onValueChange = viewModel::updateDoseAmount,
                        label = "Dose Amount",
                        placeholder = "250",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))

                    var unitMenuExpanded by remember { mutableStateOf(false) }
                    SelectionField(
                        label = "Unit",
                        value = uiState.doseUnit,
                        expanded = unitMenuExpanded,
                        onExpandChange = { unitMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        DoseUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.name) },
                                onClick = {
                                    viewModel.updateDoseUnit(unit.name)
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                when (uiState.frequencyType.toFrequencyTypeOrNull()) {
                    FrequencyType.CUSTOM -> {
                        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                        Text(
                            text = "Repeat on",
                            color = PepLogTheme.colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
                        ) {
                            listOf(
                                1 to "Sun", 2 to "Mon", 3 to "Tue", 4 to "Wed",
                                5 to "Thu", 6 to "Fri", 7 to "Sat"
                            ).forEach { (day, label) ->
                                PepLogChip(
                                    text = label,
                                    selected = day in uiState.customFrequencyDays,
                                    onClick = { viewModel.toggleCustomDay(day) }
                                )
                            }
                        }
                    }
                    FrequencyType.CYCLE -> {
                        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            PepLogTextField(
                                value = uiState.cycleOnDays,
                                onValueChange = viewModel::updateCycleOnDays,
                                label = "On days",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                            PepLogTextField(
                                value = uiState.cycleOffDays,
                                onValueChange = viewModel::updateCycleOffDays,
                                label = "Off days",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                Row(modifier = Modifier.fillMaxWidth()) {
                    var frequencyMenuExpanded by remember { mutableStateOf(false) }
                    SelectionField(
                        label = "Frequency",
                        value = uiState.frequencyType.toDisplayLabel(),
                        expanded = frequencyMenuExpanded,
                        onExpandChange = { frequencyMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        FrequencyType.entries.forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name.toDisplayLabel()) },
                                onClick = {
                                    viewModel.updateFrequency(frequency.name)
                                    frequencyMenuExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))

                    var routeMenuExpanded by remember { mutableStateOf(false) }
                    SelectionField(
                        label = "Route",
                        value = uiState.adminRoute,
                        expanded = routeMenuExpanded,
                        onExpandChange = { routeMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        AdminRoute.entries.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route.toDisplayLabel()) },
                                onClick = {
                                    viewModel.updateAdminRoute(route.name)
                                    routeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                Text(
                    text = "The next 365 days are scheduled in your local timezone. Confirm dose and route details with your clinician.",
                    color = PepLogTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                PepLogButton(
                    text = "Add to Protocol",
                    onClick = { viewModel.saveCompound(onSuccess = onCompoundAdded) },
                    isLoading = uiState.isSaving,
                    enabled = selectedPeptide != null &&
                        uiState.doseAmount.isNotBlank() &&
                        when (uiState.frequencyType.toFrequencyTypeOrNull()) {
                            FrequencyType.CUSTOM -> uiState.customFrequencyDays.isNotEmpty()
                            FrequencyType.CYCLE -> {
                                val onDays = uiState.cycleOnDays.toIntOrNull()
                                val offDays = uiState.cycleOffDays.toIntOrNull()
                                onDays in 1..365 && offDays in 0..365
                            }
                            else -> true
                        },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    menuContent: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = PepLogTheme.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Box {
            OutlinedButton(
                onClick = { onExpandChange(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(text = value, modifier = Modifier.fillMaxWidth())
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                menuContent()
            }
        }
    }
}

private fun String.toDisplayLabel(): String =
    lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun String.toFrequencyTypeOrNull(): FrequencyType? =
    runCatching { FrequencyType.valueOf(trim().uppercase()) }.getOrNull()

private fun AdminRoute.toDisplayLabel(): String = when (this) {
    AdminRoute.SUBQ -> "Subcutaneous"
    AdminRoute.IM -> "Intramuscular"
    AdminRoute.INTRANASAL -> "Intranasal"
    AdminRoute.ORAL -> "Oral"
    AdminRoute.TOPICAL -> "Topical"
}
