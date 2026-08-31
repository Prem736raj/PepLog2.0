package com.appvexis.peptidetracker.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.components.BiomarkerTrackerView
import com.appvexis.peptidetracker.feature.progress.components.BodyMetricsView
import com.appvexis.peptidetracker.feature.progress.components.PhotoGalleryView
import com.appvexis.peptidetracker.feature.progress.components.SubjectiveMetricsView
import com.appvexis.peptidetracker.feature.progress.dialogs.AddProgressPhotoDialog
import com.appvexis.peptidetracker.feature.progress.dialogs.LogBiomarkerDialog
import com.appvexis.peptidetracker.feature.progress.dialogs.LogBodyMetricsDialog
import com.appvexis.peptidetracker.feature.progress.dialogs.LogSideEffectDialog
import com.appvexis.peptidetracker.feature.progress.model.ProgressTab
import com.appvexis.peptidetracker.feature.progress.model.ProgressUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBackClick: () -> Unit = {},
    onNavigateToPhotoComparison: (beforeUri: String, afterUri: String) -> Unit = { _, _ -> },
    viewModel: ProgressViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val selectedPhotoCat by viewModel.selectedPhotoCategory.collectAsStateWithLifecycle()
    val protocols by viewModel.allProtocols.collectAsStateWithLifecycle()

    val showAddPhoto by viewModel.showAddPhotoDialog.collectAsStateWithLifecycle()
    val showLogBiomarker by viewModel.showLogBiomarkerDialog.collectAsStateWithLifecycle()
    val showLogSideEffect by viewModel.showLogSideEffectDialog.collectAsStateWithLifecycle()
    val showLogBodyMetrics by viewModel.showLogBodyMetricsDialog.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    val colors = PepLogTheme.colors

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress & Biomarkers",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeTab) {
                        ProgressTab.PHOTOS -> viewModel.showAddPhotoDialog()
                        ProgressTab.BIOMARKERS -> viewModel.showLogBiomarkerDialog()
                        ProgressTab.SUBJECTIVE -> viewModel.showLogSideEffectDialog()
                        ProgressTab.BODY_METRICS -> viewModel.showLogBodyMetricsDialog()
                    }
                },
                containerColor = colors.primary,
                contentColor = colors.background,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = when (activeTab) {
                        ProgressTab.PHOTOS -> Icons.Default.AddAPhoto
                        ProgressTab.BIOMARKERS -> Icons.Default.Science
                        ProgressTab.SUBJECTIVE -> Icons.Default.Warning
                        ProgressTab.BODY_METRICS -> Icons.Default.MonitorWeight
                    },
                    contentDescription = "Action"
                )
            }
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            PrimaryTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = colors.background,
                contentColor = colors.primary
            ) {
                ProgressTab.entries.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = tab.displayName,
                                fontFamily = OutfitFontFamily,
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (activeTab == tab) colors.primary else colors.textSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

            when (val state = uiState) {
                is ProgressUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is ProgressUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = PepLogTheme.spacing.medium)
                            .padding(bottom = 90.dp)
                    ) {
                        when (state.activeTab) {
                            ProgressTab.PHOTOS -> {
                                PhotoGalleryView(
                                    photos = state.photos,
                                    selectedCategory = state.selectedCategory,
                                    onSelectCategory = { cat -> viewModel.selectPhotoCategory(cat) },
                                    onAddPhotoClick = { viewModel.showAddPhotoDialog() },
                                    onCompareClick = { before, after ->
                                        onNavigateToPhotoComparison(before, after)
                                    },
                                    onDeletePhotoClick = { id -> viewModel.deleteProgressPhoto(id) }
                                )
                            }

                            ProgressTab.BIOMARKERS -> {
                                BiomarkerTrackerView(
                                    biomarkers = state.biomarkerLogs,
                                    onLogBiomarkerClick = { viewModel.showLogBiomarkerDialog() },
                                    onDeleteBiomarkerClick = { id -> viewModel.deleteBiomarker(id) }
                                )
                            }

                            ProgressTab.SUBJECTIVE -> {
                                SubjectiveMetricsView(
                                    latestCheckIn = state.latestCheckIn,
                                    history = state.subjectiveCheckIns,
                                    onSaveCheckIn = { mood, energy, sleep, pain, libido, notes ->
                                        viewModel.saveDailySubjectiveCheckIn(mood, energy, sleep, pain, libido, notes)
                                    },
                                    onLogSideEffectClick = { viewModel.showLogSideEffectDialog() },
                                    onDeleteSideEffect = { id -> viewModel.deleteSideEffect(id) }
                                )
                            }

                            ProgressTab.BODY_METRICS -> {
                                BodyMetricsView(
                                    metrics = state.bodyMetrics,
                                    latestWeight = state.latestWeight,
                                    totalDeltaWeight = state.weightDeltaTotal,
                                    onLogMetricsClick = { viewModel.showLogBodyMetricsDialog() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showAddPhoto) {
            AddProgressPhotoDialog(
                protocols = protocols,
                onDismiss = { viewModel.hideAddPhotoDialog() },
                onConfirm = { uri, cat, protoId, notes ->
                    viewModel.addProgressPhoto(uri, cat, protoId, notes)
                }
            )
        }

        if (showLogBiomarker) {
            LogBiomarkerDialog(
                protocols = protocols,
                onDismiss = { viewModel.hideLogBiomarkerDialog() },
                onConfirm = { name, value, unit, protoId, lab, notes ->
                    viewModel.logBiomarker(name, value, unit, protoId, lab, notes)
                }
            )
        }

        if (showLogSideEffect) {
            LogSideEffectDialog(
                protocols = protocols,
                onDismiss = { viewModel.hideLogSideEffectDialog() },
                onConfirm = { category, severity, protoId, notes ->
                    viewModel.logSideEffect(category, severity, protoId, notes)
                }
            )
        }

        if (showLogBodyMetrics) {
            LogBodyMetricsDialog(
                protocols = protocols,
                onDismiss = { viewModel.hideLogBodyMetricsDialog() },
                onConfirm = { weight, fat, muscle, waist, protoId, notes ->
                    viewModel.logBodyMetrics(weight, fat, muscle, waist, protoId, notes)
                }
            )
        }
    }
}
