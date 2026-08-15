package com.appvexis.peptidetracker.feature.health

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.health.components.CorrelationOverlayChart
import com.appvexis.peptidetracker.feature.health.components.HealthConnectPermissionCard
import com.appvexis.peptidetracker.feature.health.components.HealthMetricCards
import com.appvexis.peptidetracker.feature.health.components.PatternInsightCards
import com.appvexis.peptidetracker.feature.health.components.getMetricColor
import com.appvexis.peptidetracker.feature.health.model.HealthConnectStatus
import com.appvexis.peptidetracker.feature.health.model.HealthTimeRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.isNotEmpty()) {
            viewModel.onPermissionsGranted()
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health Connect",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PepLogTheme.colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PepLogTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.permissionsGranted) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                color = PepLogTheme.colors.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = { viewModel.syncAndLoad() }) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = PepLogTheme.colors.primary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background
                )
            )
        },
        containerColor = PepLogTheme.colors.background
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = uiState.isSyncing,
            onRefresh = { viewModel.syncAndLoad() },
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission / status section
                if (!uiState.permissionsGranted) {
                    item {
                        HealthConnectPermissionCard(
                            status = uiState.healthConnectStatus,
                            onRequestPermissions = {
                                val permissions = viewModel.uiState.value.let {
                                    // launch permission request
                                    // We need the HealthConnectManager permissions
                                }
                                permissionLauncher.launch(
                                    setOf(
                                        "android.permission.health.READ_WEIGHT",
                                        "android.permission.health.READ_SLEEP",
                                        "android.permission.health.READ_HEART_RATE",
                                        "android.permission.health.READ_BLOOD_PRESSURE",
                                        "android.permission.health.READ_STEPS",
                                        "android.permission.health.READ_BODY_FAT",
                                        "android.permission.health.READ_RESTING_HEART_RATE"
                                    )
                                )
                            },
                            onInstallHealthConnect = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=com.google.android.apps.healthdata")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Data section — only when permissions are granted
                if (uiState.permissionsGranted) {
                    // Sync status bar
                    item {
                        SyncStatusBar(
                            lastSyncTime = uiState.lastSyncTime,
                            isSyncing = uiState.isSyncing
                        )
                    }

                    // Metric summary cards
                    if (uiState.metricSummaries.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = "Health Metrics",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = PepLogTheme.colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                HealthMetricCards(
                                    summaries = uiState.metricSummaries,
                                    selectedType = uiState.selectedMetricType,
                                    onTypeSelected = { viewModel.selectMetricType(it) }
                                )
                            }
                        }
                    }

                    // Time range selector
                    item {
                        TimeRangeSelector(
                            selectedRange = uiState.selectedTimeRange,
                            onRangeSelected = { viewModel.selectTimeRange(it) }
                        )
                    }

                    // Correlation overlay chart
                    item {
                        PepLogCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isGlassmorphic = true
                        ) {
                            CorrelationOverlayChart(
                                dataPoints = uiState.chartData,
                                doseMarkers = uiState.doseMarkers,
                                metricUnit = uiState.selectedMetricType.unit,
                                metricName = uiState.selectedMetricType.displayName,
                                curveColor = getMetricColor(uiState.selectedMetricType),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Pattern insights
                    if (uiState.patterns.isNotEmpty()) {
                        item {
                            PatternInsightCards(
                                patterns = uiState.patterns
                            )
                        }
                    }

                    // Empty state
                    if (uiState.metricSummaries.isEmpty() && !uiState.isSyncing) {
                        item {
                            EmptyHealthState(onSync = { viewModel.syncAndLoad() })
                        }
                    }

                    // Bottom spacer
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBar(
    lastSyncTime: Long?,
    isSyncing: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSyncing) PepLogTheme.colors.secondary
                        else PepLogTheme.colors.success
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSyncing) "Syncing..." else "Connected",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PepLogTheme.colors.textSecondary,
                fontFamily = OutfitFontFamily
            )
        }

        if (lastSyncTime != null) {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                text = "Last sync: ${dateFormat.format(Date(lastSyncTime))}",
                fontSize = 11.sp,
                color = PepLogTheme.colors.textSecondary.copy(alpha = 0.7f),
                fontFamily = OutfitFontFamily
            )
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: HealthTimeRange,
    onRangeSelected: (HealthTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HealthTimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) PepLogTheme.colors.primary.copy(alpha = 0.15f)
                        else PepLogTheme.colors.surface.copy(alpha = 0.6f)
                    )
                    .then(
                        Modifier.padding(vertical = 8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.label,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) PepLogTheme.colors.primary
                    else PepLogTheme.colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyHealthState(
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Health Data Yet",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = PepLogTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap the sync button to import data from Health Connect",
            fontSize = 13.sp,
            color = PepLogTheme.colors.textSecondary.copy(alpha = 0.7f),
            fontFamily = OutfitFontFamily,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 18.sp
        )
    }
}
