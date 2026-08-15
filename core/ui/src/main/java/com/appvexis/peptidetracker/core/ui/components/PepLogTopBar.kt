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

/**
 * Premium top bar v2 — supports center-aligned and left-aligned variants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PepLogTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    isCenterAligned: Boolean = true
) {
    val colors = PepLogTheme.colors
    
    val titleComposable = @Composable {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
    }

    if (isCenterAligned) {
        CenterAlignedTopAppBar(
            title = titleComposable,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colors.background,
                titleContentColor = colors.textPrimary,
                navigationIconContentColor = colors.textPrimary,
                actionIconContentColor = colors.textSecondary
            ),
            modifier = modifier
        )
    } else {
        TopAppBar(
            title = titleComposable,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.background,
                titleContentColor = colors.textPrimary,
                navigationIconContentColor = colors.textPrimary,
                actionIconContentColor = colors.textSecondary
            ),
            modifier = modifier
        )
    }
}
