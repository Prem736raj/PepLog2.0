package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.model.DashboardRoute
import com.appvexis.peptidetracker.core.model.InsightsRoute
import com.appvexis.peptidetracker.core.model.MoreRoute
import com.appvexis.peptidetracker.core.model.ProtocolsRoute
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Tab definitions for the PepLog bottom navigation bar.
 */
enum class PepLogTab(
    val route: Any,
    val icon: ImageVector,
    val label: String
) {
    DASHBOARD(DashboardRoute, Icons.Outlined.SpaceDashboard, "Today"),
    PROTOCOLS(ProtocolsRoute, Icons.Outlined.Assignment, "Protocols"),
    INSIGHTS(InsightsRoute, Icons.Outlined.Analytics, "Insights"),
    MORE(MoreRoute, Icons.Outlined.MoreHoriz, "More")
}

/**
 * Premium animated bottom navigation bar v2.
 *
 * Features:
 * - Gradient sliding active indicator with glow
 * - Spring bounce animation on tab selection
 * - Frosted glass background effect
 * - Smooth text expand/collapse
 */
@Composable
fun AnimatedBottomBar(
    currentRoute: Any?,
    onTabSelected: (PepLogTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIndex = PepLogTab.entries.indexOfFirst {
        val routeName = it.route::class.simpleName ?: ""
        currentRoute?.toString()?.contains(routeName) == true
    }.let { 
        if (it == -1) {
            if (currentRoute?.toString()?.contains("EncyclopediaRoute") == true) {
                3 // Highlight 'More' tab
            } else {
                0
            }
        } else {
            it
        }
    }

    // Smooth sliding indicator
    val offsetFraction by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sliding_indicator"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false,
                ambientColor = PepLogTheme.colors.primary.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
    ) {
        // Frosted glass background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (PepLogTheme.colors.isDark) {
                        PepLogTheme.colors.surface.copy(alpha = 0.92f)
                    } else {
                        PepLogTheme.colors.surface.copy(alpha = 0.96f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (PepLogTheme.colors.isDark) {
                        Color.White.copy(alpha = 0.06f)
                    } else {
                        Color.Black.copy(alpha = 0.04f)
                    },
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // Tabs container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp)
        ) {
            val tabWidth = maxWidth / PepLogTab.entries.size
            val lineWidth = 24.dp

            // Gradient sliding underline with glow
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (tabWidth * offsetFraction) + (tabWidth - lineWidth) / 2)
                    .width(lineWidth)
                    .height(3.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(1.5.dp),
                        ambientColor = PepLogTheme.colors.primary.copy(alpha = 0.5f),
                        spotColor = PepLogTheme.colors.primary.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PepLogTheme.colors.primaryGradientStart,
                                PepLogTheme.colors.primaryGradientEnd
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PepLogTab.entries.forEachIndexed { index, tab ->
                    val isSelected = index == activeIndex

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "icon_color"
                    )

                    // Bounce animation for selected icon
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "icon_scale"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(tabWidth)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTabSelected(tab)
                            }
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(iconScale)
                        )

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.label,
                                    color = iconColor,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
