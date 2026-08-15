package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
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
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Onboarding empty state shown to new users without active protocols or scheduled doses.
 */
@Composable
fun DashboardEmptyState(
    onCreateProtocol: () -> Unit,
    onExploreEncyclopedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                Text(
                    text = "Welcome to PepLog",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Track your stacks, reconstitution dilution, dose schedules, site rotations, and 28-day expiration countdowns with precision.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                PepLogButton(
                    text = "Create Your First Protocol",
                    onClick = onCreateProtocol,
                    variant = PepLogButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PepLogButton(
                    text = "Explore Peptide Library",
                    onClick = onExploreEncyclopedia,
                    variant = PepLogButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

        // Protocol Inspiration Header
        Text(
            text = "Popular Protocol Stacks",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        InspirationItem(
            title = "Wolverine Recovery Stack",
            compounds = "BPC-157 (500 mcg/day) + TB-500 (2.5 mg/week)",
            goal = "Tissue Repair & Tendon Healing",
            onClick = onCreateProtocol
        )

        Spacer(modifier = Modifier.height(8.dp))

        InspirationItem(
            title = "Growth Hormone Optimization",
            compounds = "CJC-1295 (100 mcg) + Ipamorelin (200 mcg) before bed",
            goal = "Sleep Quality & Recovery",
            onClick = onCreateProtocol
        )

        Spacer(modifier = Modifier.height(8.dp))

        InspirationItem(
            title = "GLP-1 Metabolic Titration",
            compounds = "Semaglutide / Tirzepatide (Weekly escalating dose)",
            goal = "Metabolic Health & Appetite Regulation",
            onClick = onCreateProtocol
        )
    }
}

@Composable
private fun InspirationItem(
    title: String,
    compounds: String,
    goal: String,
    onClick: () -> Unit
) {
    val colors = PepLogTheme.colors

    PepLogCard(
        onClick = onClick,
        isGlassmorphic = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = compounds,
                        fontSize = 12.sp,
                        color = colors.primary
                    )
                    Text(
                        text = "Goal: $goal",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
