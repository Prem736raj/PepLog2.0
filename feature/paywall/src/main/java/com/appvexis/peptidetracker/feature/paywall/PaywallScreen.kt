package com.appvexis.peptidetracker.feature.paywall

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogTag
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.paywall.model.PlanDisplay

/** A truthful, low-pressure purchase screen with Play Store pricing as the source of truth. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBackClick: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = PepLogTheme.colors

    if (uiState.isPremium) {
        PremiumActiveScreen(onBackClick = onBackClick)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PepLog Premium") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::restorePurchases, enabled = !uiState.isLoading) {
                        Text("Restore")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
        containerColor = colors.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PepLogBrandMark(size = 56.dp)
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "More room for your records",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Premium expands PepLog’s local tracking and review tools. Prices, trials, and renewal terms are always confirmed by Google Play.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colors.textSecondary,
                )
            }

            Spacer(Modifier.height(24.dp))
            FeatureSummary()
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Choose a plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            uiState.plans.forEach { plan ->
                PlanOption(
                    plan = plan,
                    selected = plan.plan == uiState.selectedPlan,
                    onSelect = { viewModel.selectPlan(plan.plan) },
                )
                Spacer(Modifier.height(10.dp))
            }

            uiState.error?.let { error ->
                PepLogCard(isGlassmorphic = true, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.accent,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            val selected = uiState.plans.firstOrNull { it.plan == uiState.selectedPlan }
            val activity = androidx.compose.ui.platform.LocalContext.current as? Activity
            PepLogButton(
                text = when {
                    uiState.isLoading -> "Loading plans"
                    selected?.price == "Unavailable" -> "Try again"
                    else -> "Continue with ${selected?.title ?: "selected plan"}"
                },
                onClick = { activity?.let(viewModel::launchPurchase) },
                modifier = Modifier.fillMaxWidth(),
                enabled = activity != null && !uiState.isLoading,
                isLoading = uiState.isLoading,
                icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) },
            )

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Your payment is handled by Google Play. Subscriptions renew unless cancelled in Google Play before the next billing period.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onNavigateToTermsOfService) { Text("Terms") }
                Text("·", color = colors.textSecondary)
                TextButton(onClick = onNavigateToPrivacyPolicy) { Text("Privacy") }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureSummary() {
    PepLogCard(isGlassmorphic = true, modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureRow("More active protocols")
            FeatureRow("Detailed reports and trend views")
            FeatureRow("PK visualizer and expanded analysis")
            FeatureRow("Health Connect trend integration")
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    val colors = PepLogTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = colors.success,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PlanOption(
    plan: PlanDisplay,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = PepLogTheme.colors
    PepLogCard(
        isGlassmorphic = selected,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    plan.badge?.takeIf(String::isNotBlank)?.let { badge ->
                        Spacer(Modifier.width(8.dp))
                        PepLogTag(text = badge)
                    }
                }
                if (plan.pricePerUnit.isNotBlank()) {
                    Text(
                        text = plan.pricePerUnit,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                plan.trialText?.takeIf(String::isNotBlank)?.let { trial ->
                    Text(
                        text = trial,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.success,
                    )
                }
            }
            Text(
                text = plan.price,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) colors.primary else colors.textPrimary,
                textAlign = TextAlign.End,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumActiveScreen(onBackClick: () -> Unit) {
    val colors = PepLogTheme.colors
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PepLog Premium") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
        containerColor = colors.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PepLogBrandMark(size = 60.dp)
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Premium is active",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your expanded tracking and review tools are available on this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            PepLogButton(text = "Back to PepLog", onClick = onBackClick)
        }
    }
}
