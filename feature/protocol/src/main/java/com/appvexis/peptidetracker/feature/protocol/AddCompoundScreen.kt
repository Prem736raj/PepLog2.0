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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomPeptideDialog by remember { mutableStateOf(false) }
    var customPeptideName by remember { mutableStateOf("") }
    var customPeptideCategory by remember { mutableStateOf("") }

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
                    DropdownMenuItem(
                        text = { Text("Add custom compound") },
                        onClick = {
                            peptideMenuExpanded = false
                            customPeptideName = ""
                            customPeptideCategory = ""
                            showCustomPeptideDialog = true
                        }
                    )
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

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                Text(
                    text = "Dose time",
                    color = PepLogTheme.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium
                )
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatDoseTime(uiState.timeOfDay), modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Titration schedule",
                            color = PepLogTheme.colors.textPrimary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Change the logged dose by week when your clinician has given you a ramp plan.",
                            color = PepLogTheme.colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = uiState.titrationEnabled,
                        onCheckedChange = viewModel::setTitrationEnabled
                    )
                }

                if (uiState.titrationEnabled) {
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                    uiState.titrationSteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PepLogTextField(
                                value = step.week,
                                onValueChange = { viewModel.updateTitrationWeek(index, it) },
                                label = "Week",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.7f)
                            )
                            Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                            PepLogTextField(
                                value = step.doseAmount,
                                onValueChange = { viewModel.updateTitrationDose(index, it) },
                                label = "Dose",
                                placeholder = uiState.doseAmount.ifBlank { "250" },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.3f)
                            )
                            if (uiState.titrationSteps.size > 1) {
                                IconButton(onClick = { viewModel.removeTitrationStep(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove week ${index + 1}",
                                        tint = PepLogTheme.colors.accent
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                    }
                    TextButton(onClick = viewModel::addTitrationStep) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add week")
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

    if (showTimePicker) {
        val initialTime = uiState.timeOfDay.toLocalTimeOrNull()
            ?: java.time.LocalTime.of(8, 0)
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose dose time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTimeOfDay(
                        "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                    )
                    showTimePicker = false
                }) {
                    Text("Use this time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    if (showCustomPeptideDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isCreatingCustomPeptide) showCustomPeptideDialog = false },
            title = { Text("Add custom compound") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Use this for an unlisted compound. PepLog will track it, but will not show a PK estimate without validated reference data.",
                        color = PepLogTheme.colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    PepLogTextField(
                        value = customPeptideName,
                        onValueChange = { customPeptideName = it },
                        label = "Name",
                        placeholder = "e.g. Retatrutide",
                        enabled = !uiState.isCreatingCustomPeptide,
                    )
                    PepLogTextField(
                        value = customPeptideCategory,
                        onValueChange = { customPeptideCategory = it },
                        label = "Category (optional)",
                        placeholder = "Custom",
                        enabled = !uiState.isCreatingCustomPeptide,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isCreatingCustomPeptide,
                    onClick = {
                        viewModel.addCustomPeptide(
                            nameInput = customPeptideName,
                            categoryInput = customPeptideCategory,
                        ) {
                            showCustomPeptideDialog = false
                        }
                    }
                ) {
                    Text(if (uiState.isCreatingCustomPeptide) "Adding…" else "Add compound")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isCreatingCustomPeptide,
                    onClick = { showCustomPeptideDialog = false }
                ) { Text("Cancel") }
            }
        )
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

private fun String.toLocalTimeOrNull(): java.time.LocalTime? = runCatching {
    java.time.LocalTime.parse(trim())
}.getOrNull()

private fun formatDoseTime(value: String): String {
    val time = value.toLocalTimeOrNull() ?: return value
    val hour = time.hour % 12
    val displayHour = if (hour == 0) 12 else hour
    val period = if (time.hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(displayHour, time.minute, period)
}
