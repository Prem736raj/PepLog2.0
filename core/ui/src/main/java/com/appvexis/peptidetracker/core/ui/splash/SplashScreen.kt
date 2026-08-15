package com.appvexis.peptidetracker.core.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animated splash screen with PepLog branding.
 *
 * Shows a teal-gradient background with an animated logo icon
 * and app name. Automatically calls [onSplashFinished] after the
 * animation completes.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // --- Animations ---
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Phase 1: Icon scales in (0 → 1) + fades in
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = EaseOutCubic,
                )
            )
        }
        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 400,
                easing = LinearEasing,
            )
        )

        // Phase 2: Title slides in
        delay(200)
        showTitle = true

        // Phase 3: Subtitle fades in
        delay(300)
        showSubtitle = true

        // Phase 4: Hold, then navigate
        delay(800)
        onSplashFinished()
    }

    // --- UI ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080B14), // true-dark OLED
                        Color(0xFF111827), // slate-900 surface
                        Color(0xFF0C3547), // dark cyan tint
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo icon
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "PepLog Logo",
                tint = Color(0xFF22D3EE), // vibrant cyan primary
                modifier = Modifier
                    .size(80.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(400, easing = EaseOutCubic),
                ),
            ) {
                Text(
                    text = "PepLog",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF0F0F5), // off-white
                    letterSpacing = 2.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(400)),
            ) {
                Text(
                    text = "Premium Peptide Tracker",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8888A0), // text secondary
                    letterSpacing = 1.sp,
                )
            }
        }

        // Version text at bottom
        AnimatedVisibility(
            visible = showSubtitle,
            enter = fadeIn(tween(600)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Text(
                text = "v1.0",
                fontSize = 12.sp,
                color = Color(0xFF555570),
                modifier = Modifier
                    .align(Alignment.BottomCenter),
            )
        }
    }
}
