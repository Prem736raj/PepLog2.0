package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Animated loading state with pulsing indicator.
 */
@Composable
fun PepLogLoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading your peptide data..."
) {
    val colors = PepLogTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "LoadingPulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoadingAlpha"
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(44.dp)
                .alpha(pulseAlpha),
            color = colors.primary,
            trackColor = colors.primary.copy(alpha = 0.12f),
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary.copy(alpha = pulseAlpha),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Animated empty state with breathing icon.
 */
@Composable
fun PepLogEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = PepLogTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "EmptyBreathing")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PepLogTheme.spacing.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .scale(breatheScale),
            tint = colors.textSecondary.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
            PepLogButton(
                text = actionText,
                onClick = onActionClick,
                variant = PepLogButtonVariant.Outlined
            )
        }
    }
}

/**
 * Animated error state with attention-grabbing icon pulse.
 */
@Composable
fun PepLogErrorState(
    title: String = "Something went wrong",
    errorMessage: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ErrorOutline,
    retryText: String? = "Try Again",
    onRetryClick: (() -> Unit)? = null
) {
    val colors = PepLogTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "ErrorPulse")

    val errorPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ErrorAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(PepLogTheme.spacing.large),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .alpha(errorPulse),
            tint = colors.accent.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        if (retryText != null && onRetryClick != null) {
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
            PepLogButton(
                text = retryText,
                onClick = onRetryClick,
                variant = PepLogButtonVariant.Primary
            )
        }
    }
}
