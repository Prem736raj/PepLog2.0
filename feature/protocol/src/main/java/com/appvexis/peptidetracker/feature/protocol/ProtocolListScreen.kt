package com.appvexis.peptidetracker.feature.protocol

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.ProtocolStatus
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolListScreen(
    onCreateClick: () -> Unit,
    onProtocolClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtocolListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Protocols",
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
                onClick = onCreateClick,
                containerColor = PepLogTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Protocol")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ProtocolListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PepLogTheme.colors.primary
                    )
                }
                is ProtocolListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = PepLogTheme.colors.accent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProtocolListUiState.Success -> {
                    if (state.protocols.isEmpty()) {
                        EmptyProtocolsState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(PepLogTheme.spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.protocols, key = { it.id }) { protocol ->
                                ProtocolItemCard(
                                    protocol = protocol,
                                    onClick = { onProtocolClick(protocol.id) }
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
private fun EmptyProtocolsState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(PepLogTheme.spacing.large)
    ) {
        Icon(
            imageVector = Icons.Default.ListAlt,
            contentDescription = null,
            tint = PepLogTheme.colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
        Text(
            text = "No Protocols Yet",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = "Create a protocol to start tracking your doses, building cycles, and logging side effects.",
            color = PepLogTheme.colors.textSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ProtocolItemCard(
    protocol: Protocol,
    onClick: () -> Unit
) {
    val df = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateStr = protocol.startDate?.let { df.format(Date(it)) } ?: "Not started"
    
    val statusColor = when (protocol.status) {
        ProtocolStatus.ACTIVE -> PepLogTheme.colors.primary
        ProtocolStatus.PAUSED -> PepLogTheme.colors.accent
        ProtocolStatus.COMPLETED -> PepLogTheme.colors.success
        ProtocolStatus.ARCHIVED -> PepLogTheme.colors.textSecondary
    }

    PepLogCard(
        onClick = onClick,
        isGlassmorphic = protocol.status == ProtocolStatus.ACTIVE,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(PepLogTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = protocol.name,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PepLogTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(PepLogTheme.spacing.small))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = protocol.status.name,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
            
            Text(
                text = "Started: $dateStr",
                color = PepLogTheme.colors.textSecondary,
                fontSize = 12.sp
            )
            
            if (!protocol.goal.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
                Text(
                    text = "Goal: ${protocol.goal}",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
