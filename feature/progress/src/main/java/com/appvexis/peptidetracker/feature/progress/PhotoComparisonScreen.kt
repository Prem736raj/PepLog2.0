package com.appvexis.peptidetracker.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.components.PhotoComparisonSlider
import com.appvexis.peptidetracker.feature.progress.model.ProgressUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoComparisonScreen(
    initialBeforeUri: String? = null,
    initialAfterUri: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: ProgressViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = PepLogTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val photos = (uiState as? ProgressUiState.Success)?.photos ?: emptyList()

    var beforeUri by remember(photos) {
        mutableStateOf(
            initialBeforeUri
                ?: photos.lastOrNull()?.photoUri
                ?: ""
        )
    }

    var afterUri by remember(photos) {
        mutableStateOf(
            initialAfterUri
                ?: photos.firstOrNull()?.photoUri
                ?: ""
        )
    }

    var selectingTarget by remember { mutableStateOf<String?>("before") } // "before" or "after"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Before / After Slider",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
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
                actions = {
                    IconButton(onClick = {
                        val temp = beforeUri
                        beforeUri = afterUri
                        afterUri = temp
                    }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.textPrimary
                )
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = PepLogTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

            if (beforeUri.isNotBlank() && afterUri.isNotBlank()) {
                // Interactive Slider
                PhotoComparisonSlider(
                    beforePhotoUri = beforeUri,
                    afterPhotoUri = afterUri,
                    beforeLabel = "BEFORE",
                    afterLabel = "AFTER",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "↔️ Drag handle to compare • Double tap to center",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select two photos below to start comparison",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

            // Selector Control: Choose which slot to pick
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectingTarget == "before") colors.secondary.copy(alpha = 0.18f) else colors.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectingTarget = "before" }
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEFORE PHOTO",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.secondary
                        )
                        Text(
                            text = if (selectingTarget == "before") "▼ Tap thumbnail below" else "Tap to change",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectingTarget == "after") colors.primary.copy(alpha = 0.18f) else colors.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectingTarget = "after" }
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AFTER PHOTO",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = colors.primary
                        )
                        Text(
                            text = if (selectingTarget == "after") "▼ Tap thumbnail below" else "Tap to change",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

            // Thumbnails strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    val isChosenBefore = photo.photoUri == beforeUri
                    val isChosenAfter = photo.photoUri == afterUri

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceHigh)
                            .border(
                                width = if (isChosenBefore || isChosenAfter) 2.5.dp else 0.dp,
                                color = if (isChosenBefore) colors.secondary else if (isChosenAfter) colors.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (selectingTarget == "before") {
                                    beforeUri = photo.photoUri
                                    selectingTarget = "after"
                                } else {
                                    afterUri = photo.photoUri
                                    selectingTarget = "before"
                                }
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo.photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isChosenBefore || isChosenAfter) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isChosenBefore) colors.secondary else colors.primary,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(3.dp)
                            ) {
                                Text(
                                    text = if (isChosenBefore) "B" else "A",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
