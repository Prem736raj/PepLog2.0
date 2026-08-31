package com.appvexis.peptidetracker.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isOnboardingCompleted) {
        if (uiState.isOnboardingCompleted) {
            onOnboardingCompleted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PepLogTheme.colors.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Static Rich Aesthetic Decorative Background
            OnboardingBackground(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Pager Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> OnboardingWelcomePage()
                        1 -> OnboardingGoalsPage(
                            selectedGoals = uiState.selectedGoals,
                            onGoalToggled = { viewModel.toggleGoal(it) }
                        )
                        2 -> OnboardingSafetyPage()
                        3 -> OnboardingTrialPage()
                    }
                }

                // Bottom Navigation Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    if (pagerState.currentPage > 0) {
                        PepLogButton(
                            text = "Back",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            variant = PepLogButtonVariant.Ghost,
                            modifier = Modifier.width(90.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(90.dp))
                    }

                    // Page Indicator
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val active = pagerState.currentPage == index
                            val width by animateDpAsState(targetValue = if (active) 20.dp else 8.dp, label = "dotWidth")
                            val color = if (active) PepLogTheme.colors.primary else PepLogTheme.colors.textSecondary.copy(alpha = 0.3f)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .background(color, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    // Next / Get Started Button
                    val isLastPage = pagerState.currentPage == 3
                    PepLogButton(
                        text = if (isLastPage) "Get Started" else "Next",
                        onClick = {
                            coroutineScope.launch {
                                if (isLastPage) {
                                    viewModel.completeOnboarding()
                                } else {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.width(if (isLastPage) 120.dp else 90.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingWelcomePage() {
    OnboardingPageLayout(
        imageResId = R.drawable.ic_onboarding_welcome,
        title = "Track with Precision",
        subtitle = "Log protocols, visualize dose decay, and review health markers in one local-first app."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingGoalsPage(
    selectedGoals: Set<OnboardingGoal>,
    onGoalToggled: (OnboardingGoal) -> Unit
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // SVG Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_onboarding_goals),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.2f),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Text & Content Area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.6f, fill = false)
        ) {
            Text(
                text = "Define Your Goals",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Select the target areas you want to optimize. We'll tailor reference ranges, educational materials, and dashboard analytics for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // FlowRow of goal chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                OnboardingGoal.entries.forEach { goal ->
                    PepLogChip(
                        text = goal.title,
                        selected = selectedGoals.contains(goal),
                        onClick = { onGoalToggled(goal) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingSafetyPage() {
    OnboardingPageLayout(
        imageResId = R.drawable.ic_onboarding_safety,
        title = "Reconstitution & Site Safety",
        subtitle = "Take the guesswork out of dosage. Calculate exact syringe units, monitor reconstituted vial degradation (28-day limit), and use the body map assistant to log rotations and minimize local irritation."
    )
}

@Composable
private fun OnboardingTrialPage() {
    OnboardingPageLayout(
        imageResId = R.drawable.ic_onboarding_trial,
        title = "Start with a clear plan",
        subtitle = "Track protocols, doses, inventory, health trends, and reference information in one offline-first workspace. Any trial offer, price, and renewal terms are shown by Google Play before purchase."
    )
}

@Composable
private fun OnboardingPageLayout(
    imageResId: Int,
    title: String,
    subtitle: String
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // SVG Image Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.2f),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // Text Area centered at the bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f, fill = false)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Premium, senior-level framing background using overlapping, parallel fluid waves
 * drawn with smooth cubic Bézier curves and diagonal gradients. The two wave layers
 * run parallel with a 20.dp offset, creating a beautiful double-border effect.
 * No stroke outlines.
 */
@Composable
private fun OnboardingBackground(
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val offset = 20.dp.toPx()

        // --- TOP WAVE 1 (Back Layer - peeks out below Front Layer) ---
        val topPath1 = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, height * 0.07f + offset)
            cubicTo(
                x1 = width * 0.35f, y1 = height * 0.11f + offset,
                x2 = width * 0.65f, y2 = height * 0.03f + offset,
                x3 = width, y3 = height * 0.06f + offset
            )
            lineTo(width, 0f)
            close()
        }
        drawPath(
            path = topPath1,
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.28f),
                    colors.primaryVariant.copy(alpha = 0.08f)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height * 0.11f + offset)
            )
        )

        // --- TOP WAVE 2 (Front Layer - sits on top) ---
        val topPath2 = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, height * 0.07f)
            cubicTo(
                x1 = width * 0.35f, y1 = height * 0.11f,
                x2 = width * 0.65f, y2 = height * 0.03f,
                x3 = width, y3 = height * 0.06f
            )
            lineTo(width, 0f)
            close()
        }
        drawPath(
            path = topPath2,
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.14f),
                    colors.secondary.copy(alpha = 0.02f)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height * 0.11f)
            )
        )

        // --- BOTTOM WAVE 1 (Back Layer - peeks out above Front Layer) ---
        val bottomPath1 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.93f - offset)
            cubicTo(
                x1 = width * 0.35f, y1 = height * 0.89f - offset,
                x2 = width * 0.65f, y2 = height * 0.97f - offset,
                x3 = width, y3 = height * 0.94f - offset
            )
            lineTo(width, height)
            close()
        }
        drawPath(
            path = bottomPath1,
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.08f),
                    colors.primaryVariant.copy(alpha = 0.28f)
                ),
                start = Offset(0f, height * 0.89f - offset),
                end = Offset(width, height)
            )
        )

        // --- BOTTOM WAVE 2 (Front Layer - sits on top) ---
        val bottomPath2 = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.93f)
            cubicTo(
                x1 = width * 0.35f, y1 = height * 0.89f,
                x2 = width * 0.65f, y2 = height * 0.97f,
                x3 = width, y3 = height * 0.94f
            )
            lineTo(width, height)
            close()
        }
        drawPath(
            path = bottomPath2,
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.primary.copy(alpha = 0.02f),
                    colors.primary.copy(alpha = 0.14f)
                ),
                start = Offset(0f, height * 0.89f),
                end = Offset(width, height)
            )
        )
    }
}
