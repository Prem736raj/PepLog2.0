package com.appvexis.peptidetracker.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.components.ActiveProtocolsSection
import com.appvexis.peptidetracker.feature.dashboard.components.DashboardEmptyState
import com.appvexis.peptidetracker.feature.dashboard.components.NextDoseCard
import com.appvexis.peptidetracker.feature.dashboard.components.QuickActionGrid
import com.appvexis.peptidetracker.feature.dashboard.components.StreakCard
import com.appvexis.peptidetracker.feature.dashboard.components.TodayDoseSummaryCard
import com.appvexis.peptidetracker.feature.dashboard.dialogs.QuickLogDoseDialog
import com.appvexis.peptidetracker.feature.dashboard.model.DashboardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCreateProtocol: () -> Unit = {},
    onNavigateToProtocolDetail: (String) -> Unit = {},
    onNavigateToCalculator: () -> Unit = {},
    onNavigateToInjectionSites: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToEncyclopedia: () -> Unit = {},
    onNavigateToDailyLog: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val showQuickLog by viewModel.showQuickLogDialog.collectAsStateWithLifecycle()
    val allPeptides by viewModel.allPeptides.collectAsStateWithLifecycle()

    val colors = PepLogTheme.colors

    // Time-based greeting
    val greeting = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            colors.primary,
                                            colors.primaryVariant
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vaccines,
                                contentDescription = null,
                                tint = colors.background,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(10.dp))
                        Column {
                            Text(
                                text = greeting,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Your Peptide Dashboard",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Dashboard",
                            tint = colors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showQuickLogDialog() },
                containerColor = colors.primary,
                contentColor = colors.background,
                shape = CircleShape,
                modifier = Modifier.shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = colors.primary.copy(alpha = 0.35f),
                    spotColor = colors.primary.copy(alpha = 0.25f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Log Dose"
                )
            }
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is DashboardUiState.Success -> {
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = PepLogTheme.spacing.medium,
                            end = PepLogTheme.spacing.medium,
                            top = PepLogTheme.spacing.small,
                            bottom = 90.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
                    ) {
                        if (state.isEmptyState) {
                            item(key = "empty_state") {
                                DashboardEmptyState(
                                    onCreateProtocol = onNavigateToCreateProtocol,
                                    onExploreEncyclopedia = onNavigateToEncyclopedia
                                )
                            }
                            item(key = "quick_actions_empty") {
                                QuickActionGrid(
                                    onQuickLogDose = { viewModel.showQuickLogDialog() },
                                    onNavigateToCalculator = onNavigateToCalculator,
                                    onNavigateToCreateProtocol = onNavigateToCreateProtocol,
                                    onNavigateToInjectionSites = onNavigateToInjectionSites,
                                    onNavigateToInventory = onNavigateToInventory,
                                    onNavigateToEncyclopedia = onNavigateToEncyclopedia
                                )
                            }
                        } else {
                            // 1. Next Dose Countdown (if any)
                            if (state.nextDose != null) {
                                item(key = "next_dose_hero") {
                                    NextDoseCard(
                                        nextDose = state.nextDose,
                                        onLogDose = { doseId -> viewModel.markDoseAsTaken(doseId) }
                                    )
                                }
                            }

                            // 2. Today's Dose Summary & Progress Ring
                            item(key = "today_dose_summary") {
                                TodayDoseSummaryCard(
                                    todayDoses = state.todayDoses,
                                    takenCount = state.takenTodayDoses,
                                    totalCount = state.totalTodayDoses,
                                    adherencePercent = state.todayAdherencePercent,
                                    onMarkTaken = { doseId -> viewModel.markDoseAsTaken(doseId) },
                                    onViewDailyLog = onNavigateToDailyLog
                                )
                            }

                            // 3. Adherence Streak Card
                            item(key = "streak_card") {
                                StreakCard(streakInfo = state.streakInfo)
                            }

                            // 4. Active Protocols Overview
                            item(key = "active_protocols") {
                                ActiveProtocolsSection(
                                    protocols = state.activeProtocols,
                                    onProtocolClick = onNavigateToProtocolDetail,
                                    onCreateProtocolClick = onNavigateToCreateProtocol
                                )
                            }

                            // 5. Quick Action Grid
                            item(key = "quick_action_grid") {
                                QuickActionGrid(
                                    onQuickLogDose = { viewModel.showQuickLogDialog() },
                                    onNavigateToCalculator = onNavigateToCalculator,
                                    onNavigateToCreateProtocol = onNavigateToCreateProtocol,
                                    onNavigateToInjectionSites = onNavigateToInjectionSites,
                                    onNavigateToInventory = onNavigateToInventory,
                                    onNavigateToEncyclopedia = onNavigateToEncyclopedia
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Log Dialog
        if (showQuickLog) {
            QuickLogDoseDialog(
                availablePeptides = allPeptides,
                onDismiss = { viewModel.hideQuickLogDialog() },
                onConfirmLog = { name, amount, unit, notes ->
                    viewModel.quickLogDose(name, amount, unit, notes)
                }
            )
        }
    }
}
