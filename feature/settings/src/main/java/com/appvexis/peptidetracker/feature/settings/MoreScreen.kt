package com.appvexis.peptidetracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

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
    modifier: Modifier = Modifier,
) {
    val sections = listOf(
        MoreSection(
            "Tools",
            listOf(
                MoreOption("Reference library", "Compound notes and research status", Icons.AutoMirrored.Filled.MenuBook, onNavigateToEncyclopedia),
                MoreOption("Dose calculator", "Dilution, concentration, and syringe units", Icons.Default.Calculate, onNavigateToCalculator),
                MoreOption("Site rotation", "Body map and previous injection sites", Icons.Default.Vaccines, onNavigateToInjection),
                MoreOption("Inventory", "Vials, remaining volume, and expiry dates", Icons.Default.Inventory2, onNavigateToInventory),
            ),
        ),
        MoreSection(
            "Review",
            listOf(
                MoreOption("Progress & biomarkers", "Photos, measurements, and lab records", Icons.Default.InsertChart, onNavigateToProgress),
                MoreOption("PK visualizer", "Estimated level curves and timing", Icons.Default.Timeline, onNavigateToPKVisualizer),
                MoreOption("Health Connect", "Optional local trend integration", Icons.Default.Favorite, onNavigateToHealthConnect),
            ),
        ),
        MoreSection(
            "Account & data",
            listOf(
                MoreOption("Export your data", "Create CSV or JSON copies", Icons.Default.CloudUpload, onNavigateToBackupSettings),
                MoreOption("PepLog Premium", "More protocols and analysis tools", Icons.Default.Star, onNavigateToPaywall),
            ),
        ),
        MoreSection(
            "About",
            listOf(
                MoreOption("Privacy policy", "Local storage, permissions, and sharing", Icons.Default.PrivacyTip, onNavigateToPrivacyPolicy),
                MoreOption("Terms of service", "Usage terms and medical disclaimer", Icons.Default.Gavel, onNavigateToTermsOfService),
            ),
        ),
    )
    val colors = PepLogTheme.colors

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "More",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
        }

        sections.forEach { section ->
            item(key = "header_${section.title}") {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(items = section.options, key = { it.title }) { option ->
                MoreOptionRow(option)
            }
            item(key = "space_${section.title}") { Spacer(Modifier.height(6.dp)) }
        }
    }
}

@Composable
private fun MoreOptionRow(option: MoreOption) {
    val colors = PepLogTheme.colors
    PepLogCard(onClick = option.onClick, modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primary.copy(alpha = 0.11f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(option.icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(option.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(option.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

private data class MoreSection(val title: String, val options: List<MoreOption>)

private data class MoreOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
