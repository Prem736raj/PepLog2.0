package com.appvexis.peptidetracker.feature.log

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import com.appvexis.peptidetracker.core.model.DoseLog
import com.appvexis.peptidetracker.core.model.DoseStatus
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLogScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val df = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val dateStr = df.format(Date(currentDate))
    
    var showManualLogDialog by remember { mutableStateOf(false) }

    val pendingCount = (uiState as? DailyLogUiState.Success)
        ?.logs
        ?.count { it.status == DoseStatus.PENDING }
        ?: 0
    val notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    if (showManualLogDialog) {
        ManualLogDialog(
            availableCompounds = (uiState as? DailyLogUiState.Success)
                ?.availableCompounds
                .orEmpty(),
            onDismiss = { showManualLogDialog = false },
            onConfirm = { compoundId, amount, unit, notes ->
                viewModel.logManualDose(compoundId, amount, unit, notes)
                showManualLogDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Logs",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PepLogTheme.colors.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showManualLogDialog = true },
                containerColor = PepLogTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Manual Log")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
        ) {
            // Date Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PepLogTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDateByDays(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = PepLogTheme.colors.primary)
                }
                
                Text(
                    text = dateStr,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                
                IconButton(onClick = { viewModel.changeDateByDays(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = PepLogTheme.colors.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            if (pendingCount > 0 && !notificationsAllowed) {
                PepLogCard(
                    modifier = Modifier.padding(horizontal = PepLogTheme.spacing.medium),
                    isGlassmorphic = true
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Want a reminder before a dose?",
                                fontWeight = FontWeight.SemiBold,
                                color = PepLogTheme.colors.textPrimary
                            )
                            Text(
                                text = "PepLog checks locally and sends an optional reminder when a scheduled dose is close.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = PepLogTheme.colors.textSecondary
                            )
                        }
                        TextButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) { Text("Enable") }
                    }
                }
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
            }

            when (val state = uiState) {
                is DailyLogUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PepLogTheme.colors.primary)
                    }
                }
                is DailyLogUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = PepLogTheme.colors.accent)
                    }
                }
                is DailyLogUiState.Success -> {
                    if (state.logs.isEmpty()) {
                        EmptyLogsState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(PepLogTheme.spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.logs, key = { it.id }) { log ->
                                SwipeableDoseItem(
                                    log = log,
                                    compoundName = state.compoundNames[log.protocolCompoundId]
                                        ?: "Unknown compound",
                                    onMarkTaken = { viewModel.markDoseAsTaken(log.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDoseItem(
    log: DoseLog,
    compoundName: String,
    onMarkTaken: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onMarkTaken()
                true
            } else {
                false
            }
        }
    )
    
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(log.scheduledTime))

    val isTaken = log.status == DoseStatus.TAKEN

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        enableDismissFromStartToEnd = !isTaken, // Can't log taken dose again via swipe
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> PepLogTheme.colors.success
                    else -> PepLogTheme.colors.surfaceHigh
                }, label = "swipeColor"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Log Dose",
                    tint = Color.White
                )
            }
        }
    ) {
        PepLogCard(
            isGlassmorphic = !isTaken,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isTaken) PepLogTheme.colors.success.copy(alpha = 0.2f) else PepLogTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTaken) Icons.Default.DoneAll else Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = if (isTaken) PepLogTheme.colors.success else PepLogTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = compoundName,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isTaken) PepLogTheme.colors.textSecondary else PepLogTheme.colors.textPrimary
                    )
                    Text(
                        text = "${log.doseAmount} ${log.doseUnit.name.lowercase()}",
                        color = PepLogTheme.colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeStr,
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isTaken) {
                        Text(
                            text = "Logged",
                            color = PepLogTheme.colors.success,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLogsState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(PepLogTheme.spacing.large),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = "No Doses Scheduled",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = "You have no doses scheduled for this date.",
            color = PepLogTheme.colors.textSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
