package com.appvexis.peptidetracker.feature.dashboard.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/** A practical starting point that makes no treatment or outcome claims. */
@Composable
fun DashboardEmptyState(
    onCreateProtocol: () -> Unit,
    onQuickStart: () -> Unit,
    onExploreEncyclopedia: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PepLogTheme.colors
    PepLogCard(modifier = modifier.fillMaxWidth(), isGlassmorphic = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PepLogBrandMark(size = 56.dp)
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Nothing scheduled yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Start with the details you already have. PepLog keeps your records, timing, and notes together.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(20.dp))
            PepLogButton(
                text = "Set up a first reminder",
                onClick = onQuickStart,
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
            Spacer(Modifier.height(8.dp))
            PepLogButton(
                text = "Create an advanced protocol",
                onClick = onCreateProtocol,
                modifier = Modifier.fillMaxWidth(),
                variant = PepLogButtonVariant.Outlined,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
            Spacer(Modifier.height(8.dp))
            PepLogButton(
                text = "Browse reference notes",
                onClick = onExploreEncyclopedia,
                modifier = Modifier.fillMaxWidth(),
                variant = PepLogButtonVariant.Ghost,
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
            )
        }
    }
}
