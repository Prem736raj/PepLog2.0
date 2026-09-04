package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogIcons
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/** Direct-entry actions with generous targets, not a decorative dashboard grid. */
@Composable
fun QuickActionGrid(
    onQuickLogDose: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToCreateProtocol: () -> Unit,
    onNavigateToInjectionSites: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToEncyclopedia: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    val actions = listOf(
        QuickAction(PepLogIcons.DoseLog, colors.primary, "Log a dose", "Manual entry", onQuickLogDose),
        QuickAction(Icons.Default.Calculate, colors.secondary, "Calculator", "Dilution & units", onNavigateToCalculator),
        QuickAction(Icons.Default.Add, colors.primary, "New protocol", "Create a schedule", onNavigateToCreateProtocol),
        QuickAction(Icons.Default.Place, colors.accent, "Site rotation", "Body map", onNavigateToInjectionSites),
        QuickAction(Icons.Default.List, colors.secondary, "Inventory", "Vials & supplies", onNavigateToInventory),
        QuickAction(Icons.AutoMirrored.Filled.MenuBook, colors.primary, "Reference library", "Read notes", onNavigateToEncyclopedia),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Shortcuts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(10.dp))
        actions.chunked(2).forEachIndexed { index, rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowActions.forEach { action ->
                    ActionTile(action = action, modifier = Modifier.weight(1f))
                }
            }
            if (index < actions.chunked(2).lastIndex) Spacer(Modifier.height(10.dp))
        }
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

@Composable
private fun ActionTile(action: QuickAction, modifier: Modifier = Modifier) {
    val colors = PepLogTheme.colors
    PepLogCard(
        onClick = action.onClick,
        cornerRadius = 12.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(action.tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.tint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}
