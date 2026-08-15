package com.appvexis.peptidetracker.feature.encyclopedia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogEmptyState
import com.appvexis.peptidetracker.core.ui.components.PepLogLoadingState
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

enum class EvidenceLevel(val title: String, val color: Color, val description: String) {
    CLINICAL("Clinical 🟢", Color(0xFF4CAF50), "FDA-approved or supported by published human clinical trials"),
    PRACTITIONER("Practitioner 🟡", Color(0xFFFBBF24), "Commonly used in clinical practice based on practitioner protocols"),
    PRECLINICAL("Preclinical 🔴", Color(0xFFF43F5E), "Based on animal studies or anecdotal reports; no human clinical trials")
}

/**
 * Derives the evidence level from the data-driven [researchEvidence] field.
 * Falls back to PRECLINICAL if the field is missing or unrecognized.
 */
fun Peptide.getEvidenceLevel(): EvidenceLevel {
    val evidence = researchEvidence?.lowercase() ?: return EvidenceLevel.PRECLINICAL
    return when {
        evidence.contains("approved") || evidence.contains("phase iii") || evidence.contains("phase 3") -> EvidenceLevel.CLINICAL
        evidence.contains("phase ii") || evidence.contains("phase 2") || evidence.contains("clinical") || evidence.contains("practitioner") -> EvidenceLevel.PRACTITIONER
        else -> EvidenceLevel.PRECLINICAL
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "GH Secretagogues" -> Icons.Default.Bolt
        "Healing & Recovery" -> Icons.Default.Favorite
        "Weight Loss / Metabolic" -> Icons.AutoMirrored.Filled.TrendingDown
        "Mitochondrial / Longevity" -> Icons.Default.AutoAwesome
        "Cognitive / Nootropic" -> Icons.Default.Psychology
        "Anti-Aging / Skin" -> Icons.Default.Star
        "Sexual Health" -> Icons.Default.Face
        "Immune / Thymic" -> Icons.Default.Shield
        else -> Icons.Default.Info
    }
}

@Composable
fun EncyclopediaScreen(
    onPeptideClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EncyclopediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Input Row
            PaddingBox {
                PepLogTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = "Search peptides...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PepLogTheme.colors.textSecondary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = PepLogTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                )
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Bookmarks Toggle Chip
                item {
                    PepLogChip(
                        text = "Bookmarked",
                        selected = uiState.showBookmarksOnly,
                        onClick = { viewModel.toggleBookmarkFilter() },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiState.showBookmarksOnly) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (uiState.showBookmarksOnly) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // All Categories Chip
                item {
                    PepLogChip(
                        text = "All Categories",
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) }
                    )
                }

                // Individual Category Chips
                items(uiState.categories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    PepLogChip(
                        text = category,
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(category),
                                contentDescription = null,
                                tint = if (isSelected) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            // Main Content Area
            if (uiState.isLoading) {
                PepLogLoadingState(message = "Loading Peptide Reference Database...")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = PepLogTheme.spacing.medium,
                        end = PepLogTheme.spacing.medium,
                        bottom = PepLogTheme.spacing.large
                    ),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Medical Disclaimer Banner Card
                    item {
                        MedicalDisclaimerCard()
                    }

                    if (uiState.peptides.isEmpty()) {
                        item {
                            PepLogEmptyState(
                                title = "No Peptides Found",
                                description = "Try checking your spelling, selecting another category, or resetting the bookmark filter.",
                                icon = Icons.Default.Search
                            )
                        }
                    } else {
                        items(
                            items = uiState.peptides,
                            key = { it.id }
                        ) { peptide ->
                            PeptideCard(
                                peptide = peptide,
                                onClick = { onPeptideClick(peptide.id) },
                                onBookmarkToggle = { 
                                    viewModel.toggleBookmark(peptide.id, peptide.isBookmarked) 
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaddingBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PepLogTheme.spacing.medium)
    ) {
        content()
    }
}

@Composable
private fun MedicalDisclaimerCard() {
    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(PepLogTheme.spacing.small)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Medical Disclaimer",
                tint = PepLogTheme.colors.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
            Column {
                Text(
                    text = "Medical Disclaimer Reference",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "PepLog is strictly an educational tracking tool. Dosing ranges and evidence levels represent aggregate research literature, not medical recommendations. Always consult a physician before use.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PeptideCard(
    peptide: Peptide,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evidence = peptide.getEvidenceLevel()
    
    PepLogCard(
        onClick = onClick,
        isGlassmorphic = false,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = peptide.name,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PepLogTag(
                            text = peptide.category,
                            color = PepLogTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                        PepLogTag(
                            text = evidence.title,
                            color = evidence.color
                        )
                    }
                }
                
                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (peptide.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (peptide.isBookmarked) PepLogTheme.colors.accent else PepLogTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            Text(
                text = peptide.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = PepLogTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Half-life: ${peptide.halfLifeDisplay ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PepLogTheme.colors.textSecondary
                )
                Text(
                    text = peptide.typicalFrequency ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = PepLogTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
