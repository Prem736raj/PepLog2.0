package com.appvexis.peptidetracker.feature.encyclopedia

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogEmptyState
import com.appvexis.peptidetracker.core.ui.components.PepLogLoadingState
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeptideDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PeptideDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isLoading) "Peptide Details" else uiState.peptide?.name ?: "Unknown",
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
                    uiState.peptide?.let { peptide ->
                        IconButton(onClick = { viewModel.toggleBookmark(peptide.isBookmarked) }) {
                            Icon(
                                imageVector = if (peptide.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark",
                                tint = if (peptide.isBookmarked) PepLogTheme.colors.accent else PepLogTheme.colors.textPrimary
                            )
                        }
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
            if (uiState.isLoading) {
                PepLogLoadingState(message = "Loading peptide details...")
            } else {
                val peptide = uiState.peptide
                if (peptide == null) {
                    PepLogEmptyState(
                        title = "Peptide Not Found",
                        description = "We couldn't locate this peptide in the reference database.",
                        icon = Icons.Default.Info
                    )
                } else {
                    PeptideDetailContent(peptide = peptide)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeptideDetailContent(peptide: Peptide) {
    val evidence = peptide.getEvidenceLevel()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(PepLogTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        // Name & Category Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = peptide.name,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PepLogTheme.colors.textPrimary
            )
            
            // Aliases
            if (peptide.aliases.isNotEmpty()) {
                Text(
                    text = "Also known as: ${peptide.aliases.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.extraSmall)
            ) {
                PepLogTag(
                    text = peptide.category,
                    color = PepLogTheme.colors.primary
                )
                PepLogTag(
                    text = evidence.title,
                    color = evidence.color
                )
                // Show peptide class tag if available
                peptide.peptideClass?.takeIf { it.isNotBlank() }?.let {
                    PepLogTag(
                        text = it,
                        color = PepLogTheme.colors.secondary
                    )
                }
            }
            
            // Additional tags (multi-category)
            if (peptide.tags.size > 1) {
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.extraSmall)
                ) {
                    peptide.tags.filter { it != peptide.category }.forEach { tag ->
                        PepLogTag(
                            text = "↳ $tag",
                            color = PepLogTheme.colors.textSecondary
                        )
                    }
                }
            }
        }

        // Special Disclaimer warning for Preclinical/Research compounds
        if (evidence == EvidenceLevel.PRECLINICAL) {
            ResearchWarningCard()
        }

        // Legal Status & Research Evidence Quick Banner
        peptide.legalStatus?.takeIf { it.isNotBlank() }?.let { legalStatus ->
            LegalStatusCard(
                legalStatus = legalStatus,
                researchEvidence = peptide.researchEvidence
            )
        }

        // Quick Info Metrics Grid (Admin Route, Half-life, Frequency, Dose Range)
        QuickMetricsGrid(peptide = peptide)

        // Description Section
        DetailSection(
            title = "Description",
            icon = Icons.Default.Info,
            iconColor = PepLogTheme.colors.primary
        ) {
            Text(
                text = peptide.description ?: "No description available.",
                style = MaterialTheme.typography.bodyLarge,
                color = PepLogTheme.colors.textSecondary,
                lineHeight = 24.sp
            )
        }

        // Mechanism of Action Section (NEW)
        peptide.mechanismOfAction?.takeIf { it.isNotBlank() }?.let { mechanism ->
            DetailSection(
                title = "Mechanism of Action",
                icon = Icons.Default.Settings,
                iconColor = PepLogTheme.colors.primary
            ) {
                Text(
                    text = mechanism,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        // Cycle & Onset Section (NEW)
        if (peptide.cycleRecommendation != null || peptide.onsetDays != null) {
            CycleInfoCard(peptide = peptide)
        }

        // Storage & Handling Section
        DetailSection(
            title = "Storage & Reconstitution",
            icon = Icons.Default.Thermostat,
            iconColor = PepLogTheme.colors.secondary
        ) {
            Text(
                text = peptide.storageInfo ?: "No storage info available.",
                style = MaterialTheme.typography.bodyMedium,
                color = PepLogTheme.colors.textSecondary,
                lineHeight = 22.sp
            )
        }

        // Side Effects Section (now renders as chips/list)
        DetailSection(
            title = "Potential Side Effects",
            icon = Icons.Default.Warning,
            iconColor = PepLogTheme.colors.accent
        ) {
            if (peptide.sideEffects.isEmpty()) {
                Text(
                    text = "None established.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 22.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
                ) {
                    peptide.sideEffects.forEach { effect ->
                        PepLogTag(
                            text = effect,
                            color = PepLogTheme.colors.accent.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Synergies Section (now renders as chips)
        DetailSection(
            title = "Synergistic Stacks",
            icon = Icons.Default.Loop,
            iconColor = PepLogTheme.colors.primary
        ) {
            if (peptide.synergies.isEmpty()) {
                Text(
                    text = "None commonly listed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 22.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
                ) {
                    peptide.synergies.forEach { synergy ->
                        PepLogTag(
                            text = synergy,
                            color = PepLogTheme.colors.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Contraindications Section (now renders as chips)
        DetailSection(
            title = "Contraindications",
            icon = Icons.Default.Block,
            iconColor = PepLogTheme.colors.accent
        ) {
            if (peptide.contraindications.isEmpty()) {
                Text(
                    text = "None commonly listed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 22.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
                ) {
                    peptide.contraindications.forEach { contra ->
                        PepLogTag(
                            text = contra,
                            color = PepLogTheme.colors.accent
                        )
                    }
                }
            }
        }

        // Standard Mandatory Disclaimer at the Bottom
        MandatoryMedicalDisclaimer()
        
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
            content()
        }
    }
}

@Composable
private fun QuickMetricsGrid(peptide: Peptide) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricItem(
                    label = "Admin Route",
                    value = peptide.adminRoute ?: "N/A",
                    icon = Icons.Default.Vaccines,
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "Half-life",
                    value = peptide.halfLifeDisplay ?: "N/A",
                    icon = Icons.Default.HourglassEmpty,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricItem(
                    label = "Dosing Range",
                    value = peptide.typicalDoseRange ?: "N/A",
                    icon = Icons.Default.Scale,
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = "Typical Frequency",
                    value = peptide.typicalFrequency ?: "N/A",
                    icon = Icons.Default.Loop,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PepLogTheme.colors.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PepLogTheme.colors.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = PepLogTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LegalStatusCard(
    legalStatus: String,
    researchEvidence: String?
) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = PepLogTheme.colors.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Regulatory Status",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Legal Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = PepLogTheme.colors.textSecondary
                    )
                    Text(
                        text = legalStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (legalStatus.contains("FDA-Approved", ignoreCase = true)) {
                            PepLogTheme.colors.success
                        } else if (legalStatus.contains("Banned", ignoreCase = true)) {
                            PepLogTheme.colors.accent
                        } else {
                            PepLogTheme.colors.textPrimary
                        },
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
                researchEvidence?.takeIf { it.isNotBlank() }?.let { evidence ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Evidence Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                        Text(
                            text = evidence,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleInfoCard(peptide: Peptide) {
    PepLogCard(
        isGlassmorphic = false,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Cycle & Onset",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
            ) {
                peptide.cycleRecommendation?.takeIf { it.isNotBlank() && it != "Not established" }?.let { cycle ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cycle Protocol",
                            style = MaterialTheme.typography.labelSmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                        Text(
                            text = cycle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
                peptide.onsetDays?.takeIf { it.isNotBlank() && it != "Unknown" }?.let { onset ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expected Onset",
                            style = MaterialTheme.typography.labelSmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                        Text(
                            text = "$onset days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PepLogTheme.colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResearchWarningCard() {
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
                contentDescription = "Research warning",
                tint = PepLogTheme.colors.accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
            Column {
                Text(
                    text = "🔴 Research Compound Warning",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.accent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "This compound has NOT been evaluated or approved by the FDA for human use. Dosing ranges and safety profiles are derived from animal research models or anecdotal clinical reports. Proceed with extreme caution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PepLogTheme.colors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun MandatoryMedicalDisclaimer() {
    PepLogCard(
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EDUCATIONAL PURPOSES ONLY",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PepLogTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dosing ranges, stacking synergies, and contraindications are provided strictly as historical/clinical reference aggregates. PepLog is not a substitute for professional medical counsel. Always seek advice from a licensed healthcare provider before using peptides.",
                style = MaterialTheme.typography.bodySmall,
                color = PepLogTheme.colors.textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
