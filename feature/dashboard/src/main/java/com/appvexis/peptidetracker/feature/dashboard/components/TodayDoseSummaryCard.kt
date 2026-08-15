package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.dashboard.model.TodayDoseUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Today's dose schedule overview card integrating the circular adherence progress ring
 * and interactive dose items.
 */
@Composable
fun TodayDoseSummaryCard(
    todayDoses: List<TodayDoseUiModel>,
    takenCount: Int,
    totalCount: Int,
    adherencePercent: Int,
    onMarkTaken: (doseId: String) -> Unit,
    onViewDailyLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val todayFormatted = dateFormat.format(Date())

    PepLogCard(
        isGlassmorphic = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Date + Motivational message + Circular Progress Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todayFormatted,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            totalCount == 0 -> "No scheduled doses today"
                            takenCount == totalCount -> "🎉 All doses completed today!"
                            takenCount > 0 -> "$takenCount of $totalCount completed"
                            else -> "Ready for today's protocol"
                        },
                        fontSize = 13.sp,
                        color = if (takenCount == totalCount && totalCount > 0) colors.success else colors.textSecondary
                    )
                }

                if (totalCount > 0) {
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                    AdherenceProgressRing(
                        progressPercent = adherencePercent,
                        takenDoses = takenCount,
                        totalDoses = totalCount,
                        size = 72.dp,
                        strokeWidth = 7.dp
                    )
                }
            }

            if (todayDoses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                HorizontalDivider(color = colors.surfaceHigh)
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

                // Doses List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayDoses.take(4).forEach { item ->
                        TodayDoseItemRow(
                            item = item,
                            onMarkTaken = { onMarkTaken(item.doseLog.id) }
                        )
                    }
                }

                if (todayDoses.size > 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+ ${todayDoses.size - 4} more doses scheduled",
                        fontSize = 12.sp,
                        color = colors.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayDoseItemRow(
    item: TodayDoseUiModel,
    onMarkTaken: () -> Unit
) {
    val colors = PepLogTheme.colors
    val isTaken = item.isTaken

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isTaken) colors.surfaceHigh.copy(alpha = 0.5f) else colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Status Circle Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTaken) colors.success.copy(alpha = 0.18f)
                        else if (item.isOverdue) colors.accent.copy(alpha = 0.18f)
                        else colors.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTaken) Icons.Default.Check else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isTaken) colors.success else if (item.isOverdue) colors.accent else colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = item.compoundName,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isTaken) colors.textSecondary else colors.textPrimary
                )
                Text(
                    text = "${item.doseDisplay} • ${item.timeDisplay}",
                    fontSize = 12.sp,
                    color = if (isTaken) colors.textSecondary.copy(alpha = 0.7f) else colors.primary
                )
            }
        }

        if (!isTaken) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.primary.copy(alpha = 0.15f),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                IconButton(
                    onClick = onMarkTaken,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark Taken",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.success.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Taken",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.success,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
