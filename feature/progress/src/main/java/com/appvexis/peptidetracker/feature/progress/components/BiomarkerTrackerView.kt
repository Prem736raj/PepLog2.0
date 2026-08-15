package com.appvexis.peptidetracker.feature.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerCategory
import com.appvexis.peptidetracker.feature.progress.model.BiomarkerUiModel

/**
 * View displaying logged lab test results, reference ranges, and health metrics.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiomarkerTrackerView(
    biomarkers: List<BiomarkerUiModel>,
    onLogBiomarkerClick: () -> Unit,
    onDeleteBiomarkerClick: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<BiomarkerCategory?>(null) }

    val filteredList = remember(biomarkers, searchQuery, selectedCategory) {
        biomarkers.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.log.biomarkerName.contains(searchQuery, ignoreCase = true) ||
                    (item.log.labName?.contains(searchQuery, ignoreCase = true) == true)
            val matchesCategory = selectedCategory == null || item.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search bar
        PepLogTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Search Biomarkers or Labs...",
            placeholder = "e.g. IGF-1, Glucose, Lipids, ALT"
        )

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PepLogChip(
                text = "All Categories",
                selected = selectedCategory == null,
                onClick = { selectedCategory = null }
            )

            BiomarkerCategory.entries.forEach { cat ->
                PepLogChip(
                    text = cat.displayName,
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} Results",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.textPrimary
            )

            PepLogButton(
                text = "Log Lab Result",
                onClick = onLogBiomarkerClick,
                variant = PepLogButtonVariant.Primary,
                modifier = Modifier.height(36.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        if (filteredList.isEmpty()) {
            PepLogCard(
                isGlassmorphic = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                    Text(
                        text = if (searchQuery.isNotBlank()) "No Matching Biomarkers" else "No Lab Results Logged",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Log your blood panels (IGF-1, testosterone, fasting glucose, lipids, liver enzymes) to measure real biological improvements.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                    PepLogButton(
                        text = "Log First Biomarker",
                        onClick = onLogBiomarkerClick,
                        variant = PepLogButtonVariant.Primary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredList.forEach { item ->
                    BiomarkerCardItem(
                        item = item,
                        onDeleteClick = { onDeleteBiomarkerClick(item.log.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BiomarkerCardItem(
    item: BiomarkerUiModel,
    onDeleteClick: () -> Unit
) {
    val colors = PepLogTheme.colors
    val log = item.log

    val statusColor = when (item.statusText) {
        "Optimal" -> colors.success
        "High" -> colors.accent
        "Low" -> colors.secondary
        else -> colors.primary
    }

    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = log.biomarkerName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${item.category.displayName} • ${item.formattedDate}",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value & Status Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${log.value}",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.unit,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = item.statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Reference Range Indicator
            if (item.rangeLow != null && item.rangeHigh != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Standard Ref Range: ${item.rangeLow} - ${item.rangeHigh} ${log.unit}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                val ratio = if (item.rangeHigh > item.rangeLow) {
                    ((log.value - item.rangeLow) / (item.rangeHigh - item.rangeLow)).toFloat().coerceIn(0f, 1f)
                } else 0.5f

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = colors.surfaceHigh,
                    strokeCap = StrokeCap.Round
                )
            }

            // Lab Name / Notes (if present)
            log.labName?.takeIf { it.isNotBlank() }?.let { lab ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Lab: $lab",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}
