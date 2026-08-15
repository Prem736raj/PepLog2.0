package com.appvexis.peptidetracker.feature.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.inventory.model.ExpirationStatus

/**
 * Visual badge displaying the 28-day reconstitution expiration countdown,
 * shelf life progress bar, and status indicator.
 */
@Composable
fun ExpirationCountdownBadge(
    expirationStatus: ExpirationStatus,
    daysRemaining: Int?,
    hoursRemaining: Int?,
    shelfLifeElapsedRatio: Float?,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    val (badgeBg, badgeBorder, badgeText, badgeIcon, statusLabel) = when (expirationStatus) {
        ExpirationStatus.UNRECONSTITUTED -> Tuple5(
            colors.surfaceHigh,
            Color.Transparent,
            colors.textSecondary,
            Icons.Default.Schedule,
            "Unmixed (Shelf Stable)"
        )
        ExpirationStatus.FRESH -> Tuple5(
            colors.success.copy(alpha = 0.15f),
            colors.success.copy(alpha = 0.4f),
            colors.success,
            Icons.Default.HourglassTop,
            if (daysRemaining != null) "$daysRemaining days left" else "Fresh"
        )
        ExpirationStatus.EXPIRING_SOON -> Tuple5(
            colors.secondary.copy(alpha = 0.15f),
            colors.secondary.copy(alpha = 0.5f),
            colors.secondary,
            Icons.Default.HourglassBottom,
            if (daysRemaining != null) "$daysRemaining days left" else "Expiring Soon"
        )
        ExpirationStatus.CRITICAL -> Tuple5(
            colors.accent.copy(alpha = 0.18f),
            colors.accent.copy(alpha = 0.6f),
            colors.accent,
            Icons.Default.Warning,
            if (daysRemaining != null && daysRemaining > 0) "$daysRemaining days left (Use Soon!)" else "Expiring in hours!"
        )
        ExpirationStatus.EXPIRED -> Tuple5(
            colors.accent.copy(alpha = 0.25f),
            colors.accent,
            colors.accent,
            Icons.Default.Warning,
            "Expired (Past 28 Days)"
        )
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeText,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = statusLabel,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = badgeText
                )
            }
        }

        // Mini Shelf Life Progress Bar if reconstituted
        if (shelfLifeElapsedRatio != null && expirationStatus != ExpirationStatus.UNRECONSTITUTED) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (1f - shelfLifeElapsedRatio).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = badgeText,
                trackColor = colors.surfaceHigh,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
