package com.appvexis.peptidetracker.feature.pkcurves.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.pkcurves.model.CompoundCurveData

/**
 * Compound legend with color indicators and visibility toggle.
 * Tapping a row toggles that compound's visibility on the chart.
 */
@Composable
fun CompoundLegend(
    compounds: List<CompoundCurveData>,
    onToggleVisibility: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PepLogTheme.colors.surface)
            .padding(PepLogTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Compound Legend",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PepLogTheme.colors.textSecondary
        )

        compounds.forEach { compound ->
            val rowAlpha = if (compound.isVisible) 1f else 0.4f
            val bgColor by animateColorAsState(
                targetValue = if (compound.isVisible)
                    compound.color.copy(alpha = 0.08f)
                else
                    PepLogTheme.colors.surfaceHigh.copy(alpha = 0.3f),
                animationSpec = tween(300),
                label = "legendBg"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onToggleVisibility(compound.compoundId) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .alpha(rowAlpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(compound.color)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Compound name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = compound.peptideName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = PepLogTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "t½ = ${formatHalfLife(compound.halfLifeHours)} · ${compound.doseAmountMg} ${compound.doseUnit}",
                        fontSize = 11.sp,
                        color = PepLogTheme.colors.textSecondary,
                        maxLines = 1
                    )
                }

                // Current level
                if (compound.isVisible) {
                    Text(
                        text = formatConcentrationDisplay(compound.currentLevel),
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = compound.color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Visibility toggle
                Icon(
                    imageVector = if (compound.isVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = if (compound.isVisible) "Hide" else "Show",
                    tint = if (compound.isVisible) compound.color
                    else PepLogTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Summary info cards showing peak/trough values.
 */
@Composable
fun PKInfoCards(
    compounds: List<CompoundCurveData>,
    modifier: Modifier = Modifier
) {
    val visible = compounds.filter { it.isVisible && it.markers.isNotEmpty() }
    if (visible.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PepLogTheme.colors.surface)
            .padding(PepLogTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Current Levels",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PepLogTheme.colors.textSecondary
        )

        visible.forEach { compound ->
            val peak = compound.markers.filter { it.isPeak }.maxByOrNull { it.concentration }
            val trough = compound.markers.filter { !it.isPeak }.minByOrNull { it.concentration }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(compound.color.copy(alpha = 0.06f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = compound.peptideName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Now: ${formatConcentrationDisplay(compound.currentLevel)}",
                        fontSize = 11.sp,
                        color = compound.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (peak != null) {
                        Text(
                            text = "Peak: ${formatConcentrationDisplay(peak.concentration)}",
                            fontSize = 11.sp,
                            color = PepLogTheme.colors.success
                        )
                    }
                    if (trough != null) {
                        Text(
                            text = "Trough: ${formatConcentrationDisplay(trough.concentration)}",
                            fontSize = 11.sp,
                            color = PepLogTheme.colors.accent
                        )
                    }
                }
            }
        }
    }
}

private fun formatHalfLife(hours: Double): String {
    return when {
        hours >= 48.0 -> "${(hours / 24.0).toInt()}d"
        hours >= 24.0 -> "%.1fd".format(hours / 24.0)
        hours >= 1.0 -> "${hours.toInt()}h"
        else -> "${(hours * 60).toInt()}m"
    }
}

private fun formatConcentrationDisplay(value: Double): String {
    return when {
        value >= 100.0 -> "${value.toInt()} mg"
        value >= 1.0 -> "%.1f mg".format(value)
        value >= 0.01 -> "%.2f mg".format(value)
        value > 0.0 -> "< 0.01 mg"
        else -> "0 mg"
    }
}
