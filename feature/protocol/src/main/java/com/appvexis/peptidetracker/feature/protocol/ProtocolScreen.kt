package com.appvexis.peptidetracker.feature.protocol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@Composable
fun ProtocolScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        PepLogCard(
            modifier = Modifier.padding(PepLogTheme.spacing.medium),
            isGlassmorphic = false
        ) {
            Column(
                modifier = Modifier.padding(PepLogTheme.spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My protocols",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                Text(
                    text = "Organize your personal schedules, optional titration notes, reminders, and cycle dates here.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = PepLogTheme.colors.textSecondary
                )
            }
        }
    }
}
