package com.appvexis.peptidetracker.feature.progress.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.PhotoCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Visual photo gallery view with category filtering, selection, and before/after launch actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoGalleryView(
    photos: List<ProgressPhoto>,
    selectedCategory: PhotoCategory,
    onSelectCategory: (PhotoCategory) -> Unit,
    onAddPhotoClick: () -> Unit,
    onCompareClick: (beforeUri: String, afterUri: String) -> Unit,
    onDeletePhotoClick: (photoId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    var isCompareMode by remember { mutableStateOf(false) }
    val selectedPhotoIds = remember { mutableStateListOf<String>() }

    val filteredPhotos = remember(photos, selectedCategory) {
        if (selectedCategory == PhotoCategory.ALL) photos
        else photos.filter { it.category?.equals(selectedCategory.name, ignoreCase = true) == true }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Toolbar: Category Chips & Compare Mode Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${photos.size} Photos",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textPrimary
            )

            if (photos.size >= 2) {
                PepLogChip(
                    text = if (isCompareMode) "Cancel Compare" else "Compare Before/After",
                    selected = isCompareMode,
                    onClick = {
                        isCompareMode = !isCompareMode
                        selectedPhotoIds.clear()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.small))

        // Category Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PhotoCategory.entries.forEach { category ->
                PepLogChip(
                    text = category.displayName,
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

        // Floating Compare Bar when 2 photos are selected
        AnimatedVisibility(visible = isCompareMode && selectedPhotoIds.size == 2) {
            val photo1 = photos.firstOrNull { it.id == selectedPhotoIds.getOrNull(0) }
            val photo2 = photos.firstOrNull { it.id == selectedPhotoIds.getOrNull(1) }

            if (photo1 != null && photo2 != null) {
                val (before, after) = if (photo1.date <= photo2.date) photo1 to photo2 else photo2 to photo1

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "2 Photos Selected",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${dateFormat.format(Date(before.date))} ➔ ${dateFormat.format(Date(after.date))}",
                                fontSize = 11.sp,
                                color = colors.primary
                            )
                        }

                        PepLogButton(
                            text = "Compare Slider",
                            onClick = { onCompareClick(before.photoUri, after.photoUri) },
                            variant = PepLogButtonVariant.Primary,
                            modifier = Modifier.height(38.dp),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Empty state or Photos Grid
        if (filteredPhotos.isEmpty()) {
            PepLogCard(
                isGlassmorphic = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                    Text(
                        text = "No Progress Photos Yet",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Take consistent front, side, and back photos to visually track your body composition transformation.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                    PepLogButton(
                        text = "Add First Photo",
                        onClick = onAddPhotoClick,
                        variant = PepLogButtonVariant.Primary,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    val isSelected = selectedPhotoIds.contains(photo.id)

                    PhotoGridItem(
                        photo = photo,
                        isCompareMode = isCompareMode,
                        isSelected = isSelected,
                        onItemClick = {
                            if (isCompareMode) {
                                if (isSelected) selectedPhotoIds.remove(photo.id)
                                else if (selectedPhotoIds.size < 2) selectedPhotoIds.add(photo.id)
                            }
                        },
                        onDeleteClick = { onDeletePhotoClick(photo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoGridItem(
    photo: ProgressPhoto,
    isCompareMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = PepLogTheme.colors
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceHigh)
            .border(
                width = if (isSelected) 2.5.dp else 0.dp,
                color = if (isSelected) colors.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onItemClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.photoUri)
                .crossfade(true)
                .build(),
            contentDescription = "Progress photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Gradient Overlay with Date and Category Tag
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(8.dp)
        ) {
            Column {
                photo.category?.takeIf { it.isNotBlank() }?.let { cat ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colors.primary.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = dateFormat.format(Date(photo.date)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // Selection Checkbox / Delete Button (Top Right)
        if (isCompareMode) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) colors.primary else colors.background.copy(alpha = 0.7f))
                    .border(1.5.dp, Color.White, CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Photo",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
