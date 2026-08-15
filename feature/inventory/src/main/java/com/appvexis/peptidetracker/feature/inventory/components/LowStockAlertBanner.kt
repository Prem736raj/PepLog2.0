package com.appvexis.peptidetracker.feature.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * High-visibility banner displayed when peptide vials require immediate attention
 * (e.g. expiring within 3 days, expired, or volume below 0.5 mL).
 */
@Composable
fun LowStockAlertBanner(
    expiringCount: Int,
    lowVolumeCount: Int,
    onViewAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAlerts = expiringCount + lowVolumeCount
    if (totalAlerts <= 0) return

    val colors = PepLogTheme.colors
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.accent.copy(alpha = 0.12f))
            .border(1.dp, colors.accent.copy(alpha = 0.4f), shape)
            .clickable(onClick = onViewAlerts)
            .padding(PepLogTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))

                Column {
                    Text(
                        text = "Action Needed ($totalAlerts Alerts)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )

                    val description = buildString {
                        if (expiringCount > 0) {
                            append("$expiringCount vial(s) expiring soon/expired")
                        }
                        if (lowVolumeCount > 0) {
                            if (expiringCount > 0) append(" • ")
                            append("$lowVolumeCount vial(s) low on volume")
                        }
                    }

                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View alerts",
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
