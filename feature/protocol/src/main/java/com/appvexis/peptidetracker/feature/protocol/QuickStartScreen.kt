package com.appvexis.peptidetracker.feature.protocol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import com.appvexis.peptidetracker.core.model.AdminRoute
import com.appvexis.peptidetracker.core.model.DoseUnit
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickStartScreen(
    onBackClick: () -> Unit,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuickStartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val peptides by viewModel.peptides.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    var peptideMenuExpanded by remember { mutableStateOf(false) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var routeMenuExpanded by remember { mutableStateOf(false) }
    val selectedPeptide = peptides.firstOrNull { it.id == uiState.peptideId }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onSetupComplete() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    fun finishSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onSetupComplete()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "First reminder",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = PepLogTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PepLogTheme.colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background,
                ),
            )
        },
        containerColor = PepLogTheme.colors.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PepLogCard(isGlassmorphic = true, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PepLogBrandMark(size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ready in under a minute",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Choose what you already use, enter the prescribed amount, and pick a daily reminder time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary,
                        )
                    }
                }
            }

            Text(
                text = "What are you tracking?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            QuickChoiceField(
                label = "Compound",
                value = selectedPeptide?.name ?: "Choose a compound",
                expanded = peptideMenuExpanded,
                onExpandChange = { peptideMenuExpanded = it },
            ) {
                peptides.forEach { peptide ->
                    DropdownMenuItem(
                        text = { Text("${peptide.name} · ${peptide.category}") },
                        onClick = {
                            viewModel.updatePeptide(peptide.id)
                            peptideMenuExpanded = false
                        },
                    )
                }
            }
            if (peptides.isEmpty()) {
                Text(
                    text = "Loading the reference list…",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary,
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PepLogTextField(
                    value = uiState.doseAmount,
                    onValueChange = viewModel::updateDoseAmount,
                    label = "Amount",
                    placeholder = "Enter your amount",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                QuickChoiceField(
                    label = "Unit",
                    value = uiState.doseUnit.name,
                    expanded = unitMenuExpanded,
                    onExpandChange = { unitMenuExpanded = it },
                    modifier = Modifier.weight(0.8f),
                ) {
                    DoseUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = {
                                viewModel.updateDoseUnit(unit)
                                unitMenuExpanded = false
                            },
                        )
                    }
                }
            }

            QuickChoiceField(
                label = "Route",
                value = uiState.adminRoute.toDisplayLabel(),
                expanded = routeMenuExpanded,
                onExpandChange = { routeMenuExpanded = it },
            ) {
                AdminRoute.entries.forEach { route ->
                    DropdownMenuItem(
                        text = { Text(route.toDisplayLabel()) },
                        onClick = {
                            viewModel.updateRoute(route)
                            routeMenuExpanded = false
                        },
                    )
                }
            }

            Column {
                Text(
                    text = "Reminder",
                    style = MaterialTheme.typography.labelMedium,
                    color = PepLogTheme.colors.textSecondary,
                )
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Every day at ${formatDoseTime(uiState.timeOfDay)}",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            PepLogCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PepLogTheme.colors.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "PepLog will create the schedule locally. Android may ask for notification permission so the reminder can appear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary,
                    )
                }
            }

            PepLogButton(
                text = "Create reminder",
                onClick = { viewModel.createReminder(::finishSetup) },
                enabled = selectedPeptide != null && uiState.doseAmount.isNotBlank() && !uiState.isSaving,
                isLoading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            )
            Text(
                text = "You can edit frequency, titration, and other details later from the protocol.",
                style = MaterialTheme.typography.bodySmall,
                color = PepLogTheme.colors.textSecondary,
            )
        }
    }

    if (showTimePicker) {
        val initialTime = runCatching { LocalTime.parse(uiState.timeOfDay) }
            .getOrElse { LocalTime.of(8, 0) }
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Choose reminder time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateTime(
                            "%02d:%02d".format(timePickerState.hour, timePickerState.minute),
                        )
                        showTimePicker = false
                    },
                ) { Text("Use this time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun QuickChoiceField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    menuContent: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = PepLogTheme.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
        Box {
            OutlinedButton(
                onClick = { onExpandChange(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                leadingIcon?.let {
                    Icon(it, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(text = value, modifier = Modifier.fillMaxWidth())
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
            ) {
                menuContent()
            }
        }
    }
}

private fun AdminRoute.toDisplayLabel(): String = when (this) {
    AdminRoute.SUBQ -> "Subcutaneous"
    AdminRoute.IM -> "Intramuscular"
    AdminRoute.INTRANASAL -> "Intranasal"
    AdminRoute.ORAL -> "Oral"
    AdminRoute.TOPICAL -> "Topical"
}

private fun formatDoseTime(value: String): String {
    val time = runCatching { LocalTime.parse(value) }.getOrElse { LocalTime.of(8, 0) }
    val hour = time.hour % 12
    val displayHour = if (hour == 0) 12 else hour
    val period = if (time.hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(displayHour, time.minute, period)
}
