package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/** Screen titles are left-aligned by default; centered bars are reserved for short modal contexts. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PepLogTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    isCenterAligned: Boolean = false,
) {
    val colors = PepLogTheme.colors
    val titleContent: @Composable () -> Unit = {
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
    }
    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = colors.background,
        titleContentColor = colors.textPrimary,
        navigationIconContentColor = colors.textPrimary,
        actionIconContentColor = colors.textSecondary,
    )
    if (isCenterAligned) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background,
                titleContentColor = colors.textPrimary,
                navigationIconContentColor = colors.textPrimary,
                actionIconContentColor = colors.textSecondary,
            ),
            modifier = modifier,
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = barColors,
            modifier = modifier,
        )
    }
}
