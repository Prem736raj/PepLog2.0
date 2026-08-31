package com.appvexis.peptidetracker.feature.inventory.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appvexis.peptidetracker.core.model.InventoryItem
import com.appvexis.peptidetracker.core.model.InventoryStatus
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.util.UUID

/**
 * Add or Edit vial dialog with peptide selector, dosage presets, vendor & batch tracking,
 * and storage condition options.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditVialDialog(
    initialItem: InventoryItem?,
    availablePeptides: List<Peptide>,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    val isEdit = initialItem != null
    val colors = PepLogTheme.colors

    var selectedPeptideId by remember {
        mutableStateOf(initialItem?.peptideId ?: availablePeptides.firstOrNull()?.id ?: "")
    }
    var customPeptideName by remember { mutableStateOf("") }
    var vialStrengthMg by remember { mutableStateOf(initialItem?.vialStrengthMg?.toString() ?: "10") }
    var quantity by remember { mutableIntStateOf((initialItem?.quantity ?: 1).coerceIn(1, 100_000)) }
    var vendor by remember { mutableStateOf(initialItem?.vendor ?: "") }
    var batchNumber by remember { mutableStateOf(initialItem?.batchNumber ?: "") }
    var storageLocation by remember { mutableStateOf(initialItem?.storageLocation ?: "Fridge (2-8°C)") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var status by remember { mutableStateOf(initialItem?.status ?: InventoryStatus.IN_STOCK) }

    var peptideSearchQuery by remember { mutableStateOf("") }
    var showPeptidePicker by remember { mutableStateOf(false) }

    val strengthPresets = listOf("2", "5", "10", "15", "20", "50")
    val vendorSuggestions = listOf("Peptide Sciences", "Limitless Life", "Core Peptides", "Xce Peptides", "Amino Asylum")
    val storagePresets = listOf("Fridge (2-8°C)", "Freezer (-20°C)", "Cool & Dark (Room Temp)")

    val selectedPeptide = availablePeptides.find { it.id == selectedPeptideId }
    val displayPeptideName = selectedPeptide?.name ?: customPeptideName.takeIf { it.isNotBlank() } ?: "Select Peptide"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Peptide Vial" else "Add New Vial to Stock",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.textPrimary
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 1. Peptide Selector
                Text(
                    text = "Peptide Compound *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showPeptidePicker = !showPeptidePicker }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = displayPeptideName,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (selectedPeptide != null) colors.textPrimary else colors.textSecondary
                            )
                            if (selectedPeptide != null) {
                                Text(
                                    text = selectedPeptide.category,
                                    fontSize = 12.sp,
                                    color = colors.primary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Pick peptide",
                            tint = colors.primary
                        )
                    }
                }

                // Expandable Peptide Search List
                if (showPeptidePicker) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PepLogTextField(
                        value = peptideSearchQuery,
                        onValueChange = { peptideSearchQuery = it },
                        label = "Search Peptide Library",
                        placeholder = "e.g. BPC-157, Semaglutide, Tirzepatide..."
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    val filteredPeptides = availablePeptides.filter {
                        it.name.contains(peptideSearchQuery, ignoreCase = true) ||
                        it.category.contains(peptideSearchQuery, ignoreCase = true)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(4.dp)) {
                            items(filteredPeptides) { peptide ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedPeptideId = peptide.id
                                            showPeptidePicker = false
                                        }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peptide.name,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = peptide.category,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 2. Vial Strength (mg)
                Text(
                    text = "Vial Strength (mg) *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = vialStrengthMg,
                    onValueChange = { vialStrengthMg = it },
                    label = "Strength (mg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Quick strength presets
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    strengthPresets.forEach { preset ->
                        PepLogChip(
                            text = "${preset} mg",
                            selected = vialStrengthMg == preset,
                            onClick = { vialStrengthMg = preset }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 3. Quantity Stepper
                Text(
                    text = "Number of Vials",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceHigh)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = colors.textPrimary)
                    }

                    Text(
                        text = "$quantity",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )

                    IconButton(
                        onClick = { if (quantity < 100_000) quantity++ },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceHigh)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = colors.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 4. Vendor & Batch Tracking
                Text(
                    text = "Vendor & Batch Information",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = "Vendor / Supplier (Optional)",
                    placeholder = "e.g. Peptide Sciences"
                )

                // Vendor suggestions
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    vendorSuggestions.forEach { sugg ->
                        PepLogChip(
                            text = sugg,
                            selected = vendor == sugg,
                            onClick = { vendor = sugg }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                PepLogTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = "Batch / Lot Number (Optional)",
                    placeholder = "e.g. LOT-2026-08A"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 5. Storage Location
                Text(
                    text = "Storage Location",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PepLogTextField(
                    value = storageLocation,
                    onValueChange = { storageLocation = it },
                    label = "Location",
                    placeholder = "e.g. Top Fridge Shelf"
                )

                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    storagePresets.forEach { preset ->
                        PepLogChip(
                            text = preset,
                            selected = storageLocation == preset,
                            onClick = { storageLocation = preset }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // 6. Notes
                PepLogTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes / Memo (Optional)",
                    placeholder = "e.g. Certificate of Analysis verified 99.4%",
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PepLogButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = PepLogButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    )

                    val parsedStrength = vialStrengthMg.toDoubleOrNull()
                    val isValid = selectedPeptideId.isNotBlank() &&
                            parsedStrength != null &&
                            parsedStrength.isFinite() &&
                            parsedStrength > 0.0 &&
                            quantity > 0

                    PepLogButton(
                        text = if (isEdit) "Update Vial" else "Save to Stock",
                        onClick = {
                            val strength = parsedStrength ?: return@PepLogButton
                            val newItem = initialItem?.copy(
                                peptideId = selectedPeptideId,
                                vialStrengthMg = strength,
                                quantity = quantity,
                                vendor = vendor.takeIf { it.isNotBlank() },
                                batchNumber = batchNumber.takeIf { it.isNotBlank() },
                                storageLocation = storageLocation.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() },
                                status = status
                            ) ?: InventoryItem(
                                id = UUID.randomUUID().toString(),
                                peptideId = selectedPeptideId,
                                vendor = vendor.takeIf { it.isNotBlank() },
                                batchNumber = batchNumber.takeIf { it.isNotBlank() },
                                purchaseDate = System.currentTimeMillis(),
                                vialStrengthMg = strength,
                                quantity = quantity,
                                storageLocation = storageLocation.takeIf { it.isNotBlank() },
                                isReconstituted = false,
                                reconstitutionDate = null,
                                bacWaterMl = null,
                                concentrationMgMl = null,
                                expirationDate = null,
                                remainingVolumeMl = null,
                                notes = notes.takeIf { it.isNotBlank() },
                                status = InventoryStatus.IN_STOCK
                            )
                            onSave(newItem)
                        },
                        enabled = isValid,
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
