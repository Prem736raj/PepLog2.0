package com.appvexis.peptidetracker.feature.pkcurves

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.pkcurves.components.CompoundLegend
import com.appvexis.peptidetracker.feature.pkcurves.components.PKCurveCanvas
import com.appvexis.peptidetracker.feature.pkcurves.components.PKInfoCards
import com.appvexis.peptidetracker.feature.pkcurves.model.TimeWindow

/**
 * PK Half-Life Visualizer Screen.
 *
 * Displays animated pharmacokinetic decay curves for all active compounds
 * with time window switching, compound legend toggle, and crosshair interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PKVisualizerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PKVisualizerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animate the curve reveal
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(uiState.activeTimeWindow, uiState.hasData) {
        if (uiState.hasData) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    // Sync animation to VM
    LaunchedEffect(animationProgress.value) {
        viewModel.updateAnimationProgress(animationProgress.value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = PepLogTheme.colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "PK Visualizer",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PepLogTheme.colors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PepLogTheme.colors.background
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PepLogTheme.colors.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            !uiState.hasData -> {
                PKEmptyState(modifier = Modifier.fillMaxSize())
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PepLogTheme.spacing.medium)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Time Window Selector
                    TimeWindowSelector(
                        activeWindow = uiState.activeTimeWindow,
                        onWindowSelected = { viewModel.setTimeWindow(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // The PK Chart
                    PKCurveCanvas(
                        compounds = uiState.compounds,
                        timeWindow = uiState.activeTimeWindow,
                        maxConcentration = uiState.maxConcentration,
                        animationProgress = animationProgress.value,
                        onCrosshairUpdate = { viewModel.updateCrosshairTime(it) },
                        surfaceColor = PepLogTheme.colors.surface,
                        gridColor = if (PepLogTheme.colors.isDark) 
                            PepLogTheme.colors.surfaceHigh 
                        else 
                            PepLogTheme.colors.surfaceHigh.copy(alpha = 0.5f),
                        textColor = PepLogTheme.colors.textSecondary,
                        axisColor = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Cards (Peak/Trough)
                    AnimatedVisibility(
                        visible = animationProgress.value > 0.8f,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                    ) {
                        PKInfoCards(compounds = uiState.compounds)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compound Legend
                    AnimatedVisibility(
                        visible = animationProgress.value > 0.5f,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
                    ) {
                        CompoundLegend(
                            compounds = uiState.compounds,
                            onToggleVisibility = { viewModel.toggleCompoundVisibility(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Time window toggle chips row.
 */
@Composable
private fun TimeWindowSelector(
    activeWindow: TimeWindow,
    onWindowSelected: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        TimeWindow.entries.forEach { window ->
            PepLogChip(
                text = window.label,
                selected = window == activeWindow,
                onClick = { onWindowSelected(window) }
            )
        }
    }
}

/**
 * Empty state shown when no PK data is available.
 */
@Composable
private fun PKEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(PepLogTheme.spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No PK Data Yet",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = PepLogTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Log doses for compounds with known half-lives\nto see your pharmacokinetic decay curves here.",
            fontSize = 14.sp,
            color = PepLogTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Supported: BPC-157, CJC-1295, TB-500, and more",
            fontSize = 12.sp,
            color = PepLogTheme.colors.primary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
