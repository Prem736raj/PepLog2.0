package com.appvexis.peptidetracker.feature.log

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
fun DoseHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoseHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PepLogTheme.colors.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("All", "Taken", "Missed")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { 
                            selectedFilter = filter
                            viewModel.updateFilter(filter)
                        },
                        label = { Text(text = filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            when (val state = uiState) {
                is DoseHistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PepLogTheme.colors.primary)
                    }
                }
                is DoseHistoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = PepLogTheme.colors.accent)
                    }
                }
                is DoseHistoryUiState.Success -> {
                    if (state.logs.isEmpty()) {
                        EmptyHistoryState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = PepLogTheme.spacing.medium, vertical = PepLogTheme.spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.logs, key = { it.id }) { log ->
                                HistoryDoseItem(
                                    log = log,
                                    compoundName = state.compoundNames[log.protocolCompoundId]
                                        ?: "Unknown compound"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDoseItem(log: DoseLog, compoundName: String) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(log.actualTime ?: log.scheduledTime))

    val isTaken = log.status == DoseStatus.TAKEN
    val iconColor = if (isTaken) PepLogTheme.colors.success else PepLogTheme.colors.accent
    val icon = if (isTaken) Icons.Default.Done else Icons.Default.Close

    PepLogCard(
        isGlassmorphic = true,
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
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }
            
            Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = compoundName,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Text(
                    text = "${log.doseAmount} ${log.doseUnit.name.lowercase()}",
                    color = PepLogTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateStr,
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = log.status.name,
                    color = iconColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(PepLogTheme.spacing.large),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = "No History Found",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = "There are no dose logs matching the selected filter.",
            color = PepLogTheme.colors.textSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
