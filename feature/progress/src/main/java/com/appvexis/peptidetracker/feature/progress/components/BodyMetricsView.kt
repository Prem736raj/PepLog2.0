package com.appvexis.peptidetracker.feature.progress.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.BodyMetricUiModel
import java.util.Locale

/**
 * Body weight and composition tracking overview.
 */
@Composable
fun BodyMetricsView(
    metrics: List<BodyMetricUiModel>,
    latestWeight: Double?,
    totalDeltaWeight: Double?,
    onLogMetricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // Summary Cards Row
        if (latestWeight != null) {
            PepLogCard(
                isGlassmorphic = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Weight",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", latestWeight),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    if (totalDeltaWeight != null && totalDeltaWeight != 0.0) {
                        val isLoss = totalDeltaWeight < 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isLoss) colors.success.copy(alpha = 0.15f) else colors.secondary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLoss) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (isLoss) colors.success else colors.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (totalDeltaWeight > 0) "+" else ""}${String.format(Locale.US, "%.1f", totalDeltaWeight)} kg",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLoss) colors.success else colors.secondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        }

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Measurement History",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textPrimary
            )

            PepLogButton(
                text = "Log Weight",
                onClick = onLogMetricsClick,
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

        if (metrics.isEmpty()) {
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
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                    Text(
                        text = "No Body Metrics Logged",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Track weight changes, body fat %, muscle mass, and waist circumference alongside peptide protocols.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                    PepLogButton(
                        text = "Log Starting Weight",
                        onClick = onLogMetricsClick,
                        variant = PepLogButtonVariant.Primary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { item ->
                    BodyMetricItemRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun BodyMetricItemRow(item: BodyMetricUiModel) {
    val colors = PepLogTheme.colors

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
                Text(
                    text = item.formattedDate,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )

                item.deltaWeightKg?.let { delta ->
                    val isLoss = delta < 0
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isLoss) colors.success.copy(alpha = 0.15f) else colors.secondary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${if (delta > 0) "+" else ""}${String.format(Locale.US, "%.1f", delta)} kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoss) colors.success else colors.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item.weightKg?.let { w ->
                    MetricCell(label = "Weight", value = "${String.format(Locale.US, "%.1f", w)} kg")
                }
                item.bodyFatPercent?.let { fat ->
                    MetricCell(label = "Body Fat", value = "${String.format(Locale.US, "%.1f", fat)}%")
                }
                item.muscleMassKg?.let { m ->
                    MetricCell(label = "Muscle", value = "${String.format(Locale.US, "%.1f", m)} kg")
                }
                item.waistCm?.let { waist ->
                    MetricCell(label = "Waist", value = "${String.format(Locale.US, "%.1f", waist)} cm")
                }
            }

            item.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notes,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String) {
    val colors = PepLogTheme.colors
    Column {
        Text(text = label, fontSize = 10.sp, color = colors.textSecondary)
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.primary
        )
    }
}
