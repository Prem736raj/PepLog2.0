package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import kotlinx.coroutines.delay

/**
 * Quick action grid v2 with staggered fade-in entry animations.
 */
@Composable
fun QuickActionGrid(
    onQuickLogDose: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToCreateProtocol: () -> Unit,
    onNavigateToInjectionSites: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToEncyclopedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    // Staggered entry animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            ActionTile(
                icon = Icons.Default.Vaccines,
                iconTint = colors.primary,
                title = "Log Dose",
                subtitle = "Manual entry",
                onClick = onQuickLogDose,
                visible = visible,
                delayMs = 0,
                modifier = Modifier.weight(1f)
            )

            ActionTile(
                icon = Icons.Default.Calculate,
                iconTint = colors.secondary,
                title = "Calculator",
                subtitle = "Dilution & Units",
                onClick = onNavigateToCalculator,
                visible = visible,
                delayMs = 60,
                modifier = Modifier.weight(1f)
            )

            ActionTile(
                icon = Icons.Default.AddCircleOutline,
                iconTint = colors.primaryVariant,
                title = "New Protocol",
                subtitle = "Create stack",
                onClick = onNavigateToCreateProtocol,
                visible = visible,
                delayMs = 120,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small)
        ) {
            ActionTile(
                icon = Icons.Default.WaterDrop,
                iconTint = colors.primary,
                title = "Body Map",
                subtitle = "Site rotation",
                onClick = onNavigateToInjectionSites,
                visible = visible,
                delayMs = 180,
                modifier = Modifier.weight(1f)
            )

            ActionTile(
                icon = Icons.Default.Inventory2,
                iconTint = colors.secondary,
                title = "Inventory",
                subtitle = "Vials & Stocks",
                onClick = onNavigateToInventory,
                visible = visible,
                delayMs = 240,
                modifier = Modifier.weight(1f)
            )

            ActionTile(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = colors.primaryVariant,
                title = "Library",
                subtitle = "100+ Peptides",
                onClick = onNavigateToEncyclopedia,
                visible = visible,
                delayMs = 300,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    visible: Boolean,
    delayMs: Int,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    // Stagger animation per tile
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            animateIn = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "TileAlpha"
    )
    val translationY by animateFloatAsState(
        targetValue = if (animateIn) 0f else 24f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "TileTranslation"
    )

    PepLogCard(
        onClick = onClick,
        isGlassmorphic = true,
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                iconTint.copy(alpha = 0.20f),
                                iconTint.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary,
                maxLines = 1
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}
