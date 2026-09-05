package com.appvexis.peptidetracker.feature.reports

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.reports.components.AdherenceDashboardView
import com.appvexis.peptidetracker.feature.reports.components.BiomarkersTrendsView
import com.appvexis.peptidetracker.feature.reports.components.ProtocolComparisonView
import com.appvexis.peptidetracker.feature.reports.components.ReportsHeader
import com.appvexis.peptidetracker.feature.reports.components.SideEffectsTrendsView
import com.appvexis.peptidetracker.feature.reports.components.WellnessTrendsView
import com.appvexis.peptidetracker.feature.reports.model.ReportsTab

/**
 * Insights & Analytics Screen providing precomputed reports, adherence tracking,
 * symptom progression curves, biomarker shifts, subjective wellness, and protocol comparison.
 */
@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background)
    ) {
        // Top Header with Protocol Switcher, Time Filters, and Tabs
        ReportsHeader(
            protocols = uiState.allProtocols,
            selectedProtocolId = uiState.selectedProtocolId,
            onSelectProtocol = { viewModel.selectProtocol(it) },
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.selectTab(it) },
            selectedTimeRange = uiState.selectedTimeRange,
            onTimeRangeSelected = { viewModel.selectTimeRange(it) },
            onRefresh = { viewModel.recomputeAnalytics() },
            modifier = Modifier.padding(
                start = PepLogTheme.spacing.medium,
                end = PepLogTheme.spacing.medium,
                top = PepLogTheme.spacing.small
            )
        )

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PepLogTheme.colors.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            uiState.allProtocols.isEmpty() -> {
                ReportsEmptyState(modifier = Modifier.fillMaxSize())
            }

            else -> {
                // Scrollable Content per Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PepLogTheme.spacing.medium)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                        },
                        label = "tabContentTransition"
                    ) { targetTab ->
                        when (targetTab) {
                            ReportsTab.ADHERENCE -> {
                                AdherenceDashboardView(
                                    adherenceData = uiState.adherenceData
                                )
                            }
                            ReportsTab.SIDE_EFFECTS -> {
                                SideEffectsTrendsView(
                                    sideEffectsData = uiState.sideEffectsData
                                )
                            }
                            ReportsTab.BIOMARKERS -> {
                                BiomarkersTrendsView(
                                    biomarkersData = uiState.biomarkersData,
                                    onSelectBiomarker = { viewModel.selectBiomarker(it) }
                                )
                            }
                            ReportsTab.WELLNESS -> {
                                WellnessTrendsView(
                                    wellnessData = uiState.wellnessData
                                )
                            }
                            ReportsTab.COMPARISON -> {
                                ProtocolComparisonView(
                                    comparisonData = uiState.comparisonData
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
                }
            }
        }
    }
}

/**
 * Empty state shown when user has no protocols configured yet.
 */
@Composable
private fun ReportsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(PepLogTheme.spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Protocol Data Yet",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create a protocol and start logging doses, side effects, and biomarker labs to see analytics and trend charts.",
            style = MaterialTheme.typography.bodyMedium,
            color = PepLogTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
