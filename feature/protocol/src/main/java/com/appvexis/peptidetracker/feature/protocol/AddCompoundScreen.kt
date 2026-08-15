package com.appvexis.peptidetracker.feature.protocol

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompoundScreen(
    protocolId: String,
    onBackClick: () -> Unit,
    onCompoundAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddCompoundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Compound",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PepLogTheme.colors.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(PepLogTheme.spacing.medium)
            ) {
                Text(
                    text = "Configure the dosing schedule and properties for this compound.",
                    color = PepLogTheme.colors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
                
                PepLogTextField(
                    value = uiState.peptideId,
                    onValueChange = viewModel::updatePeptideId,
                    label = "Peptide Name",
                    placeholder = "e.g., BPC-157",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    PepLogTextField(
                        value = uiState.doseAmount,
                        onValueChange = viewModel::updateDoseAmount,
                        label = "Dose Amount",
                        placeholder = "250",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                    
                    PepLogTextField(
                        value = uiState.doseUnit,
                        onValueChange = viewModel::updateDoseUnit,
                        label = "Unit",
                        placeholder = "mcg",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    PepLogTextField(
                        value = uiState.frequencyType,
                        onValueChange = viewModel::updateFrequency,
                        label = "Frequency",
                        placeholder = "Daily",
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(PepLogTheme.spacing.medium))
                    
                    PepLogTextField(
                        value = uiState.adminRoute,
                        onValueChange = viewModel::updateAdminRoute,
                        label = "Route",
                        placeholder = "SubQ",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))
                
                PepLogButton(
                    text = "Add to Protocol",
                    onClick = { viewModel.saveCompound(onSuccess = onCompoundAdded) },
                    isLoading = uiState.isSaving,
                    enabled = uiState.peptideId.isNotBlank() && uiState.doseAmount.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
