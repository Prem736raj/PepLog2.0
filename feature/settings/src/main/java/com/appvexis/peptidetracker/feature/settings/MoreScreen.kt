package com.appvexis.peptidetracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip

@Composable
fun MoreScreen(
    onNavigateToEncyclopedia: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToInjection: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToProgress: () -> Unit = {},
    onNavigateToPKVisualizer: () -> Unit = {},
    onNavigateToHealthConnect: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToBackupSettings: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val options = listOf(
        MoreOption(
            title = "Progress & Biomarkers",
            description = "Photo gallery with before/after comparison slider, 50+ biomarker lab logs, daily wellness, and body composition.",
            icon = Icons.Default.InsertChart,
            onClick = onNavigateToProgress
        ),
        MoreOption(
            title = "Peptide Encyclopedia",
            description = "Reference information for 100+ compounds, research status, and caution notes.",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = onNavigateToEncyclopedia
        ),
        MoreOption(
            title = "Reconstitution Calculator",
            description = "Calculate syringe units and concentrations for bacteriostatic water dilution.",
            icon = Icons.Default.Calculate,
            onClick = onNavigateToCalculator
        ),
        MoreOption(
            title = "Injection Site Tracker",
            description = "Interactive body map to track injection sites, rotation, pain levels, and healing.",
            icon = Icons.Default.Vaccines,
            onClick = onNavigateToInjection
        ),
        MoreOption(
            title = "PK Half-Life Visualizer",
            description = "Animated pharmacokinetic decay curves showing compound blood levels over time with peak and trough markers.",
            icon = Icons.Default.Timeline,
            onClick = onNavigateToPKVisualizer
        ),
        MoreOption(
            title = "Inventory Tracker",
            description = "Track vial stocks, purchase history, batch details, remaining volume, and 28-day expiration countdowns.",
            icon = Icons.Default.Inventory2,
            onClick = onNavigateToInventory
        ),
        MoreOption(
            title = "Health Connect Sync",
            description = "Sync weight, sleep, heart rate, blood pressure, and steps for local trend views.",
            icon = Icons.Default.Favorite,
            onClick = onNavigateToHealthConnect
        ),
        MoreOption(
            title = "Backup & Export",
            description = "Export dose logs, biomarkers, protocols, and inventory as CSV or JSON. Google Drive backup requires account setup.",
            icon = Icons.Default.CloudUpload,
            onClick = onNavigateToBackupSettings
        ),
        MoreOption(
            title = "Go Premium",
            description = "Unlock unlimited protocols, advanced analytics, PK curves, Health Connect sync, and cloud backup when Drive is configured.",
            icon = Icons.Default.Star,
            onClick = onNavigateToPaywall
        ),
        MoreOption(
            title = "Privacy Policy",
            description = "How local storage, optional Google Drive backup, and sharing handle your data.",
            icon = Icons.Default.PrivacyTip,
            onClick = onNavigateToPrivacyPolicy
        ),
        MoreOption(
            title = "Terms of Service",
            description = "Usage terms, medical disclaimer, subscription info, and legal notices.",
            icon = Icons.Default.Gavel,
            onClick = onNavigateToTermsOfService
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background)
            .padding(PepLogTheme.spacing.medium)
    ) {
        Text(
            text = "More Options",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = PepLogTheme.colors.textPrimary,
            modifier = Modifier.padding(bottom = PepLogTheme.spacing.medium)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
            modifier = Modifier.fillMaxSize()
        ) {
            items(options.size) { index ->
                val option = options[index]
                PepLogCard(
                    onClick = option.onClick,
                    isGlassmorphic = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(PepLogTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PepLogTheme.colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = PepLogTheme.colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PepLogTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = option.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = PepLogTheme.colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class MoreOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
