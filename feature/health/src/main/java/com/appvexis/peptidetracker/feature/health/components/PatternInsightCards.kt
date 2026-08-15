package com.appvexis.peptidetracker.feature.health.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.HealthPattern
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * Section showing detected health + dose correlation patterns as insight cards.
 */
@Composable
fun PatternInsightCards(
    patterns: List<HealthPattern>,
    modifier: Modifier = Modifier
) {
    if (patterns.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PepLogTheme.colors.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Detected Patterns",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = PepLogTheme.colors.textPrimary
            )
        }

        patterns.forEachIndexed { index, pattern ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 100L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
            ) {
                PatternCard(pattern = pattern)
            }
        }
    }
}

@Composable
private fun PatternCard(
    pattern: HealthPattern,
    modifier: Modifier = Modifier
) {
    val borderColor = if (pattern.isPositive) {
        PepLogTheme.colors.success
    } else {
        PepLogTheme.colors.accent
    }

    val metricColor = getMetricColor(pattern.metricType)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.06f),
                        PepLogTheme.colors.surface.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Metric icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(metricColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getMetricIcon(pattern.metricType),
                        contentDescription = null,
                        tint = metricColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pattern.title,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    val protocolName = pattern.protocolName
                    if (protocolName != null) {
                        Text(
                            text = protocolName,
                            fontSize = 11.sp,
                            color = PepLogTheme.colors.textSecondary,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }

                // Change badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(borderColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pattern.changePercent > 0) {
                                Icons.AutoMirrored.Filled.TrendingUp
                            } else {
                                Icons.AutoMirrored.Filled.TrendingDown
                            },
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${String.format("%.1f", abs(pattern.changePercent))}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderColor,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pattern.description,
                fontSize = 12.sp,
                color = PepLogTheme.colors.textSecondary,
                lineHeight = 16.sp,
                fontFamily = OutfitFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Confidence",
                    fontSize = 10.sp,
                    color = PepLogTheme.colors.textSecondary.copy(alpha = 0.7f),
                    fontFamily = OutfitFontFamily
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { pattern.confidence },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = metricColor,
                    trackColor = PepLogTheme.colors.textSecondary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(pattern.confidence * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = PepLogTheme.colors.textSecondary.copy(alpha = 0.7f),
                    fontFamily = OutfitFontFamily
                )
            }
        }
    }
}
