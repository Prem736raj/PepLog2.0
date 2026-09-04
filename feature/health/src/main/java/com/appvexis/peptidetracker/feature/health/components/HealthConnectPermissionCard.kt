package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.model.HealthConnectStatus

/** Health Connect consent is presented as a clear data choice, not a promotional hero. */
@Composable
fun HealthConnectPermissionCard(
    status: HealthConnectStatus,
    onRequestPermissions: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    val title = when (status) {
        HealthConnectStatus.NOT_INSTALLED -> "Install Health Connect"
        HealthConnectStatus.NOT_SUPPORTED -> "Health Connect is unavailable"
        else -> "Connect health records"
    }
    val description = when (status) {
        HealthConnectStatus.NOT_INSTALLED ->
            "Install Health Connect from Google Play before choosing which record types PepLog may read."
        HealthConnectStatus.NOT_SUPPORTED ->
            "This device cannot provide Health Connect records. PepLog’s manual tracking remains available."
        else ->
            "Choose whether PepLog may read weight, sleep, heart rate, blood pressure, steps, and body-composition records for local trend views."
    }
    val icon = when (status) {
        HealthConnectStatus.NOT_INSTALLED -> Icons.Default.Warning
        HealthConnectStatus.NOT_SUPPORTED -> Icons.Default.Lock
        else -> Icons.Default.Favorite
    }

    PepLogCard(modifier = modifier.fillMaxWidth(), isGlassmorphic = true) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colors.primary.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = when (status) {
                            HealthConnectStatus.AVAILABLE -> "Optional permission"
                            HealthConnectStatus.NOT_INSTALLED -> "Additional Google app required"
                            else -> "No action required"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)

            when (status) {
                HealthConnectStatus.AVAILABLE -> {
                    Spacer(Modifier.height(18.dp))
                    PepLogButton(
                        text = "Review permissions",
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                HealthConnectStatus.NOT_INSTALLED -> {
                    Spacer(Modifier.height(18.dp))
                    PepLogButton(
                        text = "Open Google Play",
                        onClick = onInstallHealthConnect,
                        variant = PepLogButtonVariant.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> Unit
            }
        }
    }
}
