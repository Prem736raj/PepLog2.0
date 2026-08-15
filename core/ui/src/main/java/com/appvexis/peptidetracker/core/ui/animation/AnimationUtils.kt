package com.appvexis.peptidetracker.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Slide Up and Fade In enter transition commonly used for listing items on initial screen load.
 */
@Composable
fun SlideUpFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 400,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(durationMillis = durationMillis)
        ) + fadeIn(animationSpec = tween(durationMillis = durationMillis)),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(durationMillis = durationMillis - 100)
        ) + fadeOut(animationSpec = tween(durationMillis = durationMillis - 100)),
        modifier = modifier,
        content = { content() }
    )
}

/**
 * Scale and Fade transition designed for popups, dialogue alerts, and FAB morphs.
 */
@Composable
fun ScaleFade(
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.9f, animationSpec = tween(durationMillis = durationMillis)) + 
                fadeIn(animationSpec = tween(durationMillis = durationMillis)),
        exit = scaleOut(targetScale = 0.9f, animationSpec = tween(durationMillis = durationMillis - 100)) + 
               fadeOut(animationSpec = tween(durationMillis = durationMillis - 100)),
        modifier = modifier,
        content = { content() }
    )
}
