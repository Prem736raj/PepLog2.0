package com.appvexis.peptidetracker.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.components.ActiveProtocolsSection
import com.appvexis.peptidetracker.feature.dashboard.components.DashboardEmptyState
import com.appvexis.peptidetracker.feature.dashboard.components.NextDoseCard
import com.appvexis.peptidetracker.feature.dashboard.components.QuickActionGrid
import com.appvexis.peptidetracker.feature.dashboard.components.StreakCard
import com.appvexis.peptidetracker.feature.dashboard.components.TodayDoseSummaryCard
import com.appvexis.peptidetracker.feature.dashboard.dialogs.QuickLogDoseDialog
import com.appvexis.peptidetracker.feature.dashboard.model.DashboardUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCreateProtocol: () -> Unit = {},
    onNavigateToQuickStart: () -> Unit = {},
    onNavigateToProtocolDetail: (String) -> Unit = {},
    onNavigateToCalculator: () -> Unit = {},
    onNavigateToInjectionSites: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToEncyclopedia: () -> Unit = {},
    onNavigateToDailyLog: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val showQuickLog by viewModel.showQuickLogDialog.collectAsStateWithLifecycle()
    val colors = PepLogTheme.colors
    val currentDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = "Today",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = currentDate,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh dashboard",
                            tint = colors.textSecondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::showQuickLogDialog,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log dose") },
                containerColor = colors.primary,
                contentColor = if (colors.isDark) colors.background else androidx.compose.ui.graphics.Color.White,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                DashboardUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }

                is DashboardUiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 104.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    if (state.isEmptyState) {
                        item(key = "empty_state") {
                            DashboardEmptyState(
                                onCreateProtocol = onNavigateToCreateProtocol,
                                onQuickStart = onNavigateToQuickStart,
                                onExploreEncyclopedia = onNavigateToEncyclopedia,
                            )
                        }
                    } else {
                        state.nextDose?.let { nextDose ->
                            item(key = "next_dose") {
                                NextDoseCard(
                                    nextDose = nextDose,
                                    onLogDose = viewModel::markDoseAsTaken,
                                )
                            }
                        }

                        item(key = "today_schedule") {
                            TodayDoseSummaryCard(
                                todayDoses = state.todayDoses,
                                takenCount = state.takenTodayDoses,
                                totalCount = state.totalTodayDoses,
                                adherencePercent = state.todayAdherencePercent,
                                onMarkTaken = viewModel::markDoseAsTaken,
                                onViewDailyLog = onNavigateToDailyLog,
                            )
                        }

                        item(key = "consistency") {
                            StreakCard(streakInfo = state.streakInfo)
                        }

                        item(key = "active_protocols") {
                            ActiveProtocolsSection(
                                protocols = state.activeProtocols,
                                onProtocolClick = onNavigateToProtocolDetail,
                                onCreateProtocolClick = onNavigateToCreateProtocol,
                            )
                        }
                    }

                    item(key = "quick_actions") {
                        QuickActionGrid(
                            onQuickLogDose = viewModel::showQuickLogDialog,
                            onNavigateToCalculator = onNavigateToCalculator,
                            onNavigateToCreateProtocol = onNavigateToCreateProtocol,
                            onNavigateToInjectionSites = onNavigateToInjectionSites,
                            onNavigateToInventory = onNavigateToInventory,
                            onNavigateToEncyclopedia = onNavigateToEncyclopedia,
                        )
                    }
                }
            }
        }

        if (showQuickLog) {
            QuickLogDoseDialog(
                availableCompounds = (uiState as? DashboardUiState.Success)
                    ?.availableCompounds
                    .orEmpty(),
                onDismiss = viewModel::hideQuickLogDialog,
                onConfirmLog = { compoundId, amount, unit, notes ->
                    viewModel.quickLogDose(compoundId, amount, unit, notes)
                },
            )
        }
    }
}
