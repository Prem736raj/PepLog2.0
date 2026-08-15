package com.appvexis.peptidetracker.feature.paywall

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.paywall.model.SubscriptionPlan
import kotlinx.coroutines.delay

// Premium accent colors
private val GoldGradientStart = Color(0xFFFBBF24)
private val GoldGradientEnd = Color(0xFFE8A849)
private val PremiumPurple = Color(0xFF7C83FF)
private val PremiumTeal = Color(0xFF22D3EE)

@Composable
fun PaywallScreen(
    onBackClick: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Animate entrance
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    // If already premium, show success
    if (uiState.isPremium) {
        PremiumActiveScreen(onBackClick = onBackClick)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PepLogTheme.colors.background,
                        Color(0xFF0A0A1A),
                        Color(0xFF0F0F2A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .alpha(contentAlpha.value)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PepLogTheme.colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Restore Purchase",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = PremiumTeal,
                    modifier = Modifier.clickable { /* Restore */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium crown/star icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GoldGradientStart, GoldGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Unlock PepLog Premium",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Advanced analytics, unlimited protocols,\nand powerful health insights.",
                fontFamily = OutfitFontFamily,
                fontSize = 14.sp,
                color = PepLogTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature list
            PremiumFeaturesList()

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════════
            // PRIMARY CTA — Big Trial Button (Yearly)
            // ═══════════════════════════════════════════
            PrimaryTrialButton(
                isLoading = uiState.isLoading,
                onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        viewModel.launchTrialPurchase(activity)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle under CTA
            Text(
                text = "Then ₹699/year. Cancel anytime.",
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = PepLogTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════════════════════════════════════
            // SECONDARY OPTIONS — Weekly & Monthly
            // ═══════════════════════════════════════════
            SecondaryPlanRow(
                selectedPlan = uiState.selectedPlan,
                onPlanSelect = { plan ->
                    viewModel.selectPlan(plan)
                },
                onPurchase = {
                    val activity = context as? Activity
                    if (activity != null) {
                        viewModel.launchPurchase(activity)
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFF43F5E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Legal text
            Text(
                text = "Payment will be charged to your Google Play account. " +
                        "Subscription auto-renews unless cancelled at least 24 hours before the end of the current period.",
                fontFamily = OutfitFontFamily,
                fontSize = 10.sp,
                color = PepLogTheme.colors.textSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Terms links
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Terms of Service",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = PremiumTeal.copy(alpha = 0.7f)
                )
                Text(
                    text = "  •  ",
                    fontSize = 11.sp,
                    color = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f)
                )
                Text(
                    text = "Privacy Policy",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = PremiumTeal.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PRIMARY CTA BUTTON
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PrimaryTrialButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = tween(200),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GoldGradientStart, GoldGradientEnd, Color(0xFFE8973D))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Your 3-Day Free Trial",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECONDARY PLAN ROW (Weekly + Monthly — subtle, non-competing)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SecondaryPlanRow(
    selectedPlan: SubscriptionPlan,
    onPlanSelect: (SubscriptionPlan) -> Unit,
    onPurchase: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Or choose another plan",
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            color = PepLogTheme.colors.textSecondary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryPlanChip(
                title = "Weekly",
                price = "₹69/week",
                isSelected = selectedPlan == SubscriptionPlan.WEEKLY,
                onClick = {
                    onPlanSelect(SubscriptionPlan.WEEKLY)
                    onPurchase()
                },
                modifier = Modifier.weight(1f)
            )
            SecondaryPlanChip(
                title = "Monthly",
                price = "₹139/month",
                isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                onClick = {
                    onPlanSelect(SubscriptionPlan.MONTHLY)
                    onPurchase()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SecondaryPlanChip(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PremiumTeal.copy(alpha = 0.5f)
    else PepLogTheme.colors.textSecondary.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(
                PepLogTheme.colors.surface.copy(alpha = 0.4f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = PepLogTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = price,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = PepLogTheme.colors.textPrimary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PREMIUM FEATURES LIST
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PremiumFeaturesList() {
    val features = listOf(
        FeatureItem(Icons.Default.Science, "Unlimited Protocols", "Create and manage any number of peptide protocols"),
        FeatureItem(Icons.Default.Analytics, "Advanced Analytics", "Deep insight reports, adherence trends, and biomarker correlations"),
        FeatureItem(Icons.Default.Timeline, "PK Half-Life Curves", "Animated pharmacokinetic decay visualizer with multi-compound overlay"),
        FeatureItem(Icons.Default.Favorite, "Health Connect Sync", "Auto-sync weight, sleep, heart rate, and steps from your wearable"),
        FeatureItem(Icons.Default.Backup, "Cloud Backup", "Google Drive backup & restore with full data export"),
        FeatureItem(Icons.Default.Shield, "Priority Support", "Direct access to the development team for feedback and issues")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        features.forEachIndexed { index, feature ->
            var visible = remember { false }
            LaunchedEffect(Unit) {
                delay(100L * index)
                visible = true
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400, delayMillis = 80 * index)) +
                        slideInVertically(
                            animationSpec = tween(400, delayMillis = 80 * index),
                            initialOffsetY = { it / 4 }
                        )
            ) {
                FeatureRow(feature)
            }
        }
    }
}

@Composable
private fun FeatureRow(feature: FeatureItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PremiumTeal.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = PremiumTeal,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                text = feature.description,
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = PepLogTheme.colors.textSecondary,
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = PremiumTeal.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

// ═══════════════════════════════════════════════════════════════
// ALREADY PREMIUM — SUCCESS SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PremiumActiveScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PepLogTheme.colors.background,
                        Color(0xFF0A0A1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Back button at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PepLogTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PremiumTeal, PremiumPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You're Premium! 🎉",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "All premium features are unlocked.\nThank you for supporting PepLog!",
                fontFamily = OutfitFontFamily,
                fontSize = 14.sp,
                color = PepLogTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumTeal
                )
            ) {
                Text(
                    text = "Continue to PepLog",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
