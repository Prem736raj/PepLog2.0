package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.model.HealthConnectStatus

/**
 * Permission request card shown when Health Connect permissions haven't been granted.
 * Shows different states based on HC availability.
 */
@Composable
fun HealthConnectPermissionCard(
    status: HealthConnectStatus,
    onRequestPermissions: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PepLogTheme.colors.surface.copy(alpha = 0.9f),
                        PepLogTheme.colors.surface.copy(alpha = 0.7f)
                    )
                )
            )
            .animateContentSize()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PepLogTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (status) {
                        HealthConnectStatus.NOT_INSTALLED -> Icons.Default.Warning
                        HealthConnectStatus.NOT_SUPPORTED -> Icons.Default.Lock
                        else -> Icons.Default.Favorite
                    },
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (status) {
                    HealthConnectStatus.NOT_INSTALLED -> "Install Health Connect"
                    HealthConnectStatus.NOT_SUPPORTED -> "Health Connect Not Supported"
                    else -> "Connect Your Health Data"
                },
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = PepLogTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (status) {
                    HealthConnectStatus.NOT_INSTALLED ->
                        "Health Connect needs to be installed to sync your health data. " +
                                "It's available as a free app from Google Play."
                    HealthConnectStatus.NOT_SUPPORTED ->
                        "Your device doesn't support Health Connect. " +
                                "Health data integration requires Android 9+ with Health Connect installed."
                    else ->
                        "Grant permission to read your weight, sleep, heart rate, blood pressure, " +
                                "steps, and body composition data to see correlations with your protocols."
                },
                fontSize = 13.sp,
                color = PepLogTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                fontFamily = OutfitFontFamily
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Metric chips preview
            if (status == HealthConnectStatus.AVAILABLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    val metrics = listOf("Weight", "Sleep", "Heart Rate", "Steps", "BP")
                    metrics.forEachIndexed { index, name ->
                        if (index > 0) Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PepLogTheme.colors.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 10.sp,
                                color = PepLogTheme.colors.primary,
                                fontWeight = FontWeight.Medium,
                                fontFamily = OutfitFontFamily
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (status) {
                HealthConnectStatus.AVAILABLE -> {
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PepLogTheme.colors.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant Permissions",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
                HealthConnectStatus.NOT_INSTALLED -> {
                    Button(
                        onClick = onInstallHealthConnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PepLogTheme.colors.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Install Health Connect",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
                else -> { /* Not supported — no action possible */ }
            }
        }
    }
}
