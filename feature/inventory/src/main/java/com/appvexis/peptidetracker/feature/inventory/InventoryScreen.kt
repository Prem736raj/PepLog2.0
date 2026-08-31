package com.appvexis.peptidetracker.feature.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.components.InventorySummaryCard
import com.appvexis.peptidetracker.feature.inventory.components.LowStockAlertBanner
import com.appvexis.peptidetracker.feature.inventory.components.VialCard
import com.appvexis.peptidetracker.feature.inventory.dialogs.AddEditVialDialog
import com.appvexis.peptidetracker.feature.inventory.dialogs.AdjustVolumeDialog
import com.appvexis.peptidetracker.feature.inventory.dialogs.ReconstitutionDialog
import com.appvexis.peptidetracker.feature.inventory.dialogs.VialDetailDialog
import com.appvexis.peptidetracker.feature.inventory.model.InventoryFilter

/**
 * Main Inventory Management screen providing comprehensive stock tracking,
 * 28-day expiration countdowns, reconstitution workflow, low stock alerts, and vendor tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val allPeptides by viewModel.allPeptides.collectAsState()

    val showAddDialog by viewModel.showAddVialDialog.collectAsState()
    val vialToEdit by viewModel.vialToEdit.collectAsState()
    val vialForRecon by viewModel.selectedVialForReconstitution.collectAsState()
    val vialForAdjust by viewModel.selectedVialForVolumeAdjust.collectAsState()
    val vialForDetail by viewModel.selectedVialForDetail.collectAsState()

    val colors = PepLogTheme.colors
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.notifyCurrentAlerts()
    }
    val alertCount = (uiState as? InventoryUiState.Success)?.alertCount ?: 0

    LaunchedEffect(alertCount) {
        if (alertCount > 0 &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddEditVialDialog(
            initialItem = vialToEdit,
            availablePeptides = allPeptides,
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { viewModel.saveVial(it) }
        )
    }

    vialForRecon?.let { reconVial ->
        ReconstitutionDialog(
            vial = reconVial,
            onDismiss = { viewModel.hideReconstitutionDialog() },
            onConfirmReconstitution = { bacWater, date ->
                viewModel.reconstituteVial(reconVial.item.id, bacWater, date)
            }
        )
    }

    vialForAdjust?.let { adjustVial ->
        AdjustVolumeDialog(
            vial = adjustVial,
            onDismiss = { viewModel.hideVolumeAdjustDialog() },
            onSaveVolume = { newVol ->
                viewModel.adjustVolume(adjustVial.item.id, newVol)
            },
            onMarkEmpty = {
                viewModel.markVialAsEmpty(adjustVial.item.id)
            }
        )
    }

    vialForDetail?.let { detailVial ->
        VialDetailDialog(
            vial = detailVial,
            onDismiss = { viewModel.hideDetailDialog() },
            onReconstitute = {
                viewModel.hideDetailDialog()
                viewModel.openReconstitutionDialog(detailVial)
            },
            onAdjustVolume = {
                viewModel.hideDetailDialog()
                viewModel.openVolumeAdjustDialog(detailVial)
            },
            onEdit = {
                viewModel.hideDetailDialog()
                viewModel.openEditDialog(detailVial.item)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vial Inventory",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Vial",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = colors.primary,
                contentColor = if (colors.isDark) Color(0xFF080B14) else Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Vial",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
        ) {
            // Search Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PepLogTheme.spacing.medium)
            ) {
                PepLogTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = "Search Stock",
                    placeholder = "Peptide name, vendor, batch #...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.primary
                        )
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = colors.textSecondary
                                )
                            }
                        }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

            // Filter Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = PepLogTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InventoryFilter.entries.forEach { filter ->
                    PepLogChip(
                        text = filter.displayName,
                        selected = activeFilter == filter,
                        onClick = { viewModel.selectFilter(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            when (val state = uiState) {
                is InventoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is InventoryUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = PepLogTheme.spacing.medium,
                            end = PepLogTheme.spacing.medium,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Summary Stats Card
                        item {
                            InventorySummaryCard(
                                totalVials = state.totalVialsCount,
                                inUseCount = state.inUseCount,
                                unmixedCount = state.unmixedCount,
                                alertCount = state.alertCount
                            )
                        }

                        // Low Stock & Expiration Alert Banner
                        if (state.alertCount > 0 && activeFilter != InventoryFilter.EXPIRING_OR_LOW) {
                            item {
                                LowStockAlertBanner(
                                    expiringCount = state.expiringCount,
                                    lowVolumeCount = state.lowVolumeCount,
                                    onViewAlerts = {
                                        viewModel.selectFilter(InventoryFilter.EXPIRING_OR_LOW)
                                    }
                                )
                            }
                        }

                        // Vial Cards List or Empty State
                        if (state.vials.isEmpty()) {
                            item {
                                EmptyInventoryState(
                                    isSearching = searchQuery.isNotBlank() || activeFilter != InventoryFilter.ALL,
                                    onAddClick = { viewModel.showAddDialog() },
                                    onResetFilter = {
                                        viewModel.updateSearchQuery("")
                                        viewModel.selectFilter(InventoryFilter.ALL)
                                    }
                                )
                            }
                        } else {
                            items(state.vials, key = { it.item.id }) { vial ->
                                VialCard(
                                    vial = vial,
                                    onReconstitute = { viewModel.openReconstitutionDialog(vial) },
                                    onAdjustVolume = { viewModel.openVolumeAdjustDialog(vial) },
                                    onEdit = { viewModel.openEditDialog(vial.item) },
                                    onDelete = { viewModel.deleteVial(vial.item.id) },
                                    onCardClick = { viewModel.openDetailDialog(vial) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInventoryState(
    isSearching: Boolean,
    onAddClick: () -> Unit,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.primary.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

        Text(
            text = if (isSearching) "No Matching Vials Found" else "Your Inventory is Empty",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) {
                "No vials match your search or selected filter. Try clearing filters."
            } else {
                "Track your peptide vials, reconstitution dates, 28-day countdowns, and batch numbers in one place."
            },
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

        if (isSearching) {
            PepLogButton(
                text = "Show All Vials",
                onClick = onResetFilter,
                variant = PepLogButtonVariant.Outlined
            )
        } else {
            PepLogButton(
                text = "Add Your First Vial",
                onClick = onAddClick,
                variant = PepLogButtonVariant.Primary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}
