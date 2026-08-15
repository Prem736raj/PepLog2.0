package com.appvexis.peptidetracker.feature.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Summary bar showing high-level stats of user peptide stock.
 */
@Composable
fun InventorySummaryCard(
    totalVials: Int,
    inUseCount: Int,
    unmixedCount: Int,
    alertCount: Int,
    modifier: Modifier = Modifier
) {
    PepLogCard(
        isGlassmorphic = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                icon = Icons.Default.Inventory2,
                iconTint = PepLogTheme.colors.primary,
                count = totalVials,
                label = "Total Stock",
                modifier = Modifier.weight(1f)
            )

            SummaryItem(
                icon = Icons.Default.Science,
                iconTint = PepLogTheme.colors.primaryVariant,
                count = inUseCount,
                label = "In Use (Mixed)",
                modifier = Modifier.weight(1f)
            )

            SummaryItem(
                icon = Icons.Default.HourglassTop,
                iconTint = PepLogTheme.colors.secondary,
                count = unmixedCount,
                label = "Unmixed Dry",
                modifier = Modifier.weight(1f)
            )

            SummaryItem(
                icon = Icons.Default.Warning,
                iconTint = if (alertCount > 0) PepLogTheme.colors.accent else PepLogTheme.colors.textSecondary,
                count = alertCount,
                label = "Alerts",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    iconTint: Color,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = count.toString(),
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary
        )

        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}
