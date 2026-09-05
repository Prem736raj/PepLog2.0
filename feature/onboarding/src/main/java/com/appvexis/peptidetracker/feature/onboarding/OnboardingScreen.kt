package com.appvexis.peptidetracker.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    onStartSetup: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    var shouldStartSetup by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.isOnboardingCompleted, shouldStartSetup) {
        if (uiState.isOnboardingCompleted) {
            if (shouldStartSetup) onStartSetup() else onOnboardingCompleted()
        }
    }

    Scaffold(modifier = modifier.fillMaxSize(), containerColor = PepLogTheme.colors.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                when (page) {
                    0 -> OnboardingPage(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "Your record, in one place",
                        body = "Keep protocols, doses, vials, site notes, and personal observations together on this device.",
                        showBrand = true,
                    )
                    1 -> GoalsPage(
                        selectedGoals = uiState.selectedGoals,
                        onGoalToggled = viewModel::toggleGoal,
                    )
                    2 -> OnboardingPage(
                        icon = Icons.Default.Info,
                        title = "Plan, log, review",
                        body = "Use PepLog to organize what happened and when. It is a record-keeping tool, not medical advice or a replacement for a clinician.",
                    )
                    else -> OnboardingPage(
                        icon = Icons.Default.Lock,
                        title = "Private by default",
                        body = "Your records stay in private app storage. Every PepLog feature is free, with no subscriptions, ads, accounts, or cloud upload.",
                    )
                }
            }
            OnboardingFooter(
                page = pagerState.currentPage,
                onBack = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onStartSetup = {
                    if (!shouldStartSetup) {
                        shouldStartSetup = true
                        viewModel.completeOnboarding()
                    }
                },
                onNext = {
                    scope.launch {
                        if (pagerState.currentPage == 3) viewModel.completeOnboarding()
                        else pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    title: String,
    body: String,
    showBrand: Boolean = false,
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showBrand) {
            PepLogBrandMark(size = 64.dp)
        } else {
            Box(
                modifier = Modifier.size(64.dp).background(colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.11f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = colors.primary, modifier = Modifier.size(30.dp))
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalsPage(
    selectedGoals: Set<OnboardingGoal>,
    onGoalToggled: (OnboardingGoal) -> Unit,
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(64.dp).background(colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.11f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Star, null, tint = colors.primary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(30.dp))
        Text("Choose what you want to track", style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "These labels organize your own dashboard. They do not create recommendations or change your care.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2,
        ) {
            OnboardingGoal.entries.forEach { goal ->
                PepLogChip(
                    text = goal.title,
                    selected = goal in selectedGoals,
                    onClick = { onGoalToggled(goal) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingFooter(
    page: Int,
    onBack: () -> Unit,
    onStartSetup: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (page == 0) {
            PepLogButton(
                text = "Set up a first reminder",
                onClick = onStartSetup,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (page > 0) {
                PepLogButton("Back", onBack, variant = PepLogButtonVariant.Ghost, modifier = Modifier.width(82.dp))
            } else {
                Spacer(Modifier.width(82.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == page) 8.dp else 6.dp)
                            .background(if (index == page) colors.primary else colors.textSecondary.copy(alpha = 0.28f), androidx.compose.foundation.shape.CircleShape),
                    )
                }
            }
            PepLogButton(
                text = if (page == 3) "Start" else "Next",
                onClick = onNext,
                modifier = Modifier.width(if (page == 3) 92.dp else 82.dp),
            )
        }
    }
}
