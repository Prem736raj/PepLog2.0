package com.appvexis.peptidetracker.feature.injection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.HealingStatus
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.injection.bodymap.BodyMapBackCanvas
import com.appvexis.peptidetracker.feature.injection.bodymap.BodyMapFrontCanvas
import com.appvexis.peptidetracker.feature.injection.model.ReadinessLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionTrackerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InjectionTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isShowingFront by viewModel.isShowingFront.collectAsState()
    val selectedSite by viewModel.selectedSite.collectAsState()
    val showLogDialog by viewModel.showLogDialog.collectAsState()

    // Log dialog
    if (showLogDialog) {
        selectedSite?.let { site ->
            InjectionLogDialog(
                selectedSite = site,
                onDismiss = { viewModel.hideLogDialog() },
                onConfirm = { painLevel, healingStatus, notes ->
                    viewModel.logInjectionSite(painLevel, healingStatus, notes)
                }
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Injection Site Tracker",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PepLogTheme.colors.textPrimary
                    )
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
        },
        floatingActionButton = {
            if (selectedSite != null) {
                FloatingActionButton(
                    onClick = { viewModel.showLogDialog() },
                    containerColor = PepLogTheme.colors.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Log Injection")
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is InjectionTrackerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PepLogTheme.colors.background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PepLogTheme.colors.primary)
                }
            }
            is InjectionTrackerUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PepLogTheme.colors.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Stats Bar
                    StatsBar(
                        totalInjections = state.totalInjections,
                        recentInjections = state.recentInjections
                    )

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                    // View Toggle (Front / Back)
                    ViewToggle(
                        isShowingFront = isShowingFront,
                        onToggle = { viewModel.toggleBodyView() }
                    )

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                    // Body Map Canvas
                    PepLogCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PepLogTheme.spacing.medium),
                        isGlassmorphic = true
                    ) {
                        Column {
                            Text(
                                text = if (isShowingFront) "FRONT VIEW" else "BACK VIEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = PepLogTheme.colors.textSecondary,
                                letterSpacing = 2.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            AnimatedContent(
                                targetState = isShowingFront,
                                transitionSpec = {
                                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                                },
                                label = "bodyMapTransition"
                            ) { showFront ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.55f) // ~human body proportions
                                ) {
                                    if (showFront) {
                                        BodyMapFrontCanvas(
                                            siteStatuses = state.siteStatuses,
                                            selectedSite = selectedSite,
                                            onSiteClick = { viewModel.selectSite(it) },
                                            isDark = PepLogTheme.colors.isDark
                                        )
                                    } else {
                                        BodyMapBackCanvas(
                                            siteStatuses = state.siteStatuses,
                                            selectedSite = selectedSite,
                                            onSiteClick = { viewModel.selectSite(it) },
                                            isDark = PepLogTheme.colors.isDark
                                        )
                                    }
                                }
                            }

                            // Legend
                            MapLegend()
                        }
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                    // Selected Site Detail Panel
                    AnimatedVisibility(
                        visible = state.selectedDetail != null,
                        enter = expandVertically(tween(300)) + fadeIn(),
                        exit = shrinkVertically(tween(300)) + fadeOut()
                    ) {
                        state.selectedDetail?.let { detail ->
                            SelectedSitePanel(detail = detail)
                        }
                    }

                    // Rotation Suggestions
                    if (state.topSuggestions.isNotEmpty()) {
                        RotationSuggestionsCard(
                            suggestions = state.topSuggestions,
                            onSuggestionClick = { viewModel.selectSite(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.huge))
                }
            }
        }
    }
}

@Composable
private fun StatsBar(totalInjections: Int, recentInjections: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PepLogTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
    ) {
        StatChip(
            label = "Total",
            value = totalInjections.toString(),
            icon = Icons.Default.LocationOn,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            label = "This Week",
            value = recentInjections.toString(),
            icon = Icons.Default.Schedule,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    PepLogCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PepLogTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
            Column {
                Text(
                    text = value,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = PepLogTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ViewToggle(isShowingFront: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PepLogTheme.spacing.medium),
        horizontalArrangement = Arrangement.Center
    ) {
        PepLogButton(
            text = if (isShowingFront) "Show Back View" else "Show Front View",
            onClick = onToggle,
            variant = PepLogButtonVariant.Outlined,
            icon = {
                Icon(
                    Icons.Default.FlipCameraAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun MapLegend() {
    val colors = PepLogTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PepLogTheme.spacing.small),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = colors.accent, label = "Recent (<48h)")
        LegendItem(color = colors.warning, label = "Healing (2-7d)")
        LegendItem(color = colors.success, label = "Ready (7d+)")
        LegendItem(color = colors.textSecondary, label = "Unused")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PepLogTheme.colors.textSecondary,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun SelectedSitePanel(detail: SelectedSiteDetail) {
    val status = detail.status
    val readiness = status?.readinessLevel ?: ReadinessLevel.UNUSED

    PepLogCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PepLogTheme.spacing.medium),
        isGlassmorphic = false
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.area.displayName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    Text(
                        text = "${detail.area.category} site",
                        style = MaterialTheme.typography.bodySmall,
                        color = PepLogTheme.colors.textSecondary
                    )
                }
                ReadinessTag(readiness)
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            // Site stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat("Used", detail.totalUsageCount.toString())
                MiniStat("Days Rest", status?.daysSinceLastUse?.toString() ?: "–")
                MiniStat("Status", status?.healingStatus?.name?.lowercase()
                    ?.replaceFirstChar { it.uppercase() } ?: "OK")
            }

            // Recent history
            if (detail.recentLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                Text(
                    text = "Recent History",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                detail.recentLogs.take(5).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormat.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = PepLogTheme.colors.textSecondary
                        )
                        Row {
                            val pain = log.painLevel
                            if (pain != null) {
                                Text(
                                    text = "Pain: $pain/5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        pain <= 2 -> PepLogTheme.colors.success
                                        pain <= 3 -> PepLogTheme.colors.secondary
                                        else -> PepLogTheme.colors.accent
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (log.healingStatus != HealingStatus.OK) {
                                PepLogTag(
                                    text = log.healingStatus.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    color = PepLogTheme.colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = PepLogTheme.colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PepLogTheme.colors.textSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ReadinessTag(readiness: ReadinessLevel) {
    val (color, text) = when (readiness) {
        ReadinessLevel.RECENT -> PepLogTheme.colors.accent to "Recent"
        ReadinessLevel.HEALING -> PepLogTheme.colors.secondary to "Healing"
        ReadinessLevel.READY -> PepLogTheme.colors.success to "Ready"
        ReadinessLevel.UNUSED -> PepLogTheme.colors.textSecondary to "Unused"
    }
    PepLogTag(text = text, color = color)
}

@Composable
private fun RotationSuggestionsCard(
    suggestions: List<com.appvexis.peptidetracker.feature.injection.rotation.SuggestionResult>,
    onSuggestionClick: (com.appvexis.peptidetracker.core.model.InjectionSiteArea) -> Unit
) {
    PepLogCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PepLogTheme.spacing.medium),
        isGlassmorphic = false
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TipsAndUpdates,
                    contentDescription = null,
                    tint = PepLogTheme.colors.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Text(
                    text = "Rotation Suggestions",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            suggestions.forEachIndexed { index, suggestion ->
                PepLogCard(
                    onClick = { onSuggestionClick(suggestion.area) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index < suggestions.lastIndex) PepLogTheme.spacing.small else 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (index == 0) PepLogTheme.colors.primary.copy(alpha = 0.15f)
                                    else PepLogTheme.colors.surfaceHigh
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == 0) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Best",
                                    tint = PepLogTheme.colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PepLogTheme.colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.area.displayName,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PepLogTheme.colors.textPrimary
                            )
                            Text(
                                text = suggestion.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = PepLogTheme.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        PepLogTag(
                            text = suggestion.area.category,
                            color = PepLogTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}
