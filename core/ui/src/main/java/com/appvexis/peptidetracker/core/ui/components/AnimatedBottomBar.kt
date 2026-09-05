@file:Suppress("DEPRECATION")

package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.model.DashboardRoute
import com.appvexis.peptidetracker.core.model.InsightsRoute
import com.appvexis.peptidetracker.core.model.MoreRoute
import com.appvexis.peptidetracker.core.model.ProtocolsRoute
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

enum class PepLogTab(
    val route: Any,
    val icon: ImageVector,
    val label: String,
) {
    DASHBOARD(DashboardRoute, Icons.Outlined.Home, "Today"),
    PROTOCOLS(ProtocolsRoute, Icons.Outlined.List, "Protocols"),
    INSIGHTS(InsightsRoute, Icons.Outlined.Star, "Insights"),
    MORE(MoreRoute, Icons.Outlined.MoreVert, "More"),
}

/** Stable, labelled navigation; the app shell no longer floats or bounces above content. */
@Composable
fun AnimatedBottomBar(
    currentRoute: Any?,
    onTabSelected: (PepLogTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    val activeIndex = PepLogTab.entries.indexOfFirst { tab ->
        currentRoute?.toString()?.contains(tab.route::class.simpleName.orEmpty()) == true
    }.let { index ->
        when {
            index >= 0 -> index
            currentRoute?.toString()?.contains("EncyclopediaRoute") == true -> PepLogTab.MORE.ordinal
            else -> PepLogTab.DASHBOARD.ordinal
        }
    }
    NavigationBar(
        modifier = modifier,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        tonalElevation = 0.dp,
    ) {
        PepLogTab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == activeIndex,
                onClick = { onTabSelected(tab) },
                icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary.copy(alpha = if (colors.isDark) 0.2f else 0.12f),
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
    }
}
