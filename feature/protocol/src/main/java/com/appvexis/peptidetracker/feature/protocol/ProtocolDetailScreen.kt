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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.model.ProtocolCompound
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocolDetailScreen(
    protocolId: String,
    onBackClick: () -> Unit,
    onAddCompound: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProtocolDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState is ProtocolDetailUiState.Success) {
                        (uiState as ProtocolDetailUiState.Success).protocolWithCompounds.protocol.name
                    } else "Loading..."
                    Text(
                        text = title,
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCompound,
                containerColor = PepLogTheme.colors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Compound")
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
                is ProtocolDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PepLogTheme.colors.primary
                    )
                }
                is ProtocolDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = PepLogTheme.colors.accent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProtocolDetailUiState.Success -> {
                    val protocol = state.protocolWithCompounds.protocol
                    val compounds = state.protocolWithCompounds.compounds

                    if (compounds.isEmpty()) {
                        EmptyCompoundsState(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(PepLogTheme.spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Goal: ${protocol.goal ?: "Not specified"}",
                                    color = PepLogTheme.colors.textSecondary,
                                    modifier = Modifier.padding(bottom = PepLogTheme.spacing.medium)
                                )
                            }
                            
                            items(compounds, key = { it.id }) { compound ->
                                CompoundItemCard(
                                    compound = compound,
                                    peptideName = state.peptideNames[compound.peptideId] ?: "Unknown peptide",
                                    onDeleteClick = { viewModel.deleteCompound(compound.id) }
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
private fun EmptyCompoundsState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(PepLogTheme.spacing.large)
    ) {
        Text(
            text = "No Compounds Yet",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))
        Text(
            text = "Tap the + button to add a peptide to this protocol and set up its dosing schedule.",
            color = PepLogTheme.colors.textSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun CompoundItemCard(
    compound: ProtocolCompound,
    peptideName: String,
    onDeleteClick: () -> Unit
) {
    PepLogCard(
        isGlassmorphic = compound.isActive,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PepLogTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peptideName,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
                Text(
                    text = "${compound.doseAmount} ${compound.doseUnit} • ${compound.frequencyType}",
                    color = PepLogTheme.colors.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.extraSmall))
                Text(
                    text = "Route: ${compound.adminRoute}",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
                if (compound.titrationEnabled) {
                    Text(
                        text = "Titration Active",
                        color = PepLogTheme.colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Compound",
                    tint = PepLogTheme.colors.accent
                )
            }
        }
    }
}
