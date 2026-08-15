package com.appvexis.peptidetracker.feature.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Interactive Before/After photo comparison slider with real-time horizontal split dragging.
 */
@Composable
fun PhotoComparisonSlider(
    beforePhotoUri: String,
    afterPhotoUri: String,
    beforeLabel: String = "Before",
    afterLabel: String = "After",
    modifier: Modifier = Modifier
) {
    val colors = PepLogTheme.colors
    var sliderPositionRatio by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceHigh)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { sliderPositionRatio = 0.5f },
                    onTap = { offset ->
                        sliderPositionRatio = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val newRatio = sliderPositionRatio + (dragAmount / size.width)
                    sliderPositionRatio = newRatio.coerceIn(0.05f, 0.95f)
                }
            }
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val fullWidthDp = maxWidth
        val splitX = (totalWidthPx * sliderPositionRatio).toInt()

        // 1. Full-size "After" Photo (Background layer)
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(afterPhotoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "After photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // After Badge (Top-Right)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.background.copy(alpha = 0.82f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = afterLabel,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Clipped "Before" Photo (Foreground layer overlay)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width((fullWidthDp * sliderPositionRatio))
                .clipToBounds()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(beforePhotoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Before photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(fullWidthDp) // Maintain original full aspect width
            )

            // Before Badge (Top-Left)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.background.copy(alpha = 0.82f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = beforeLabel,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.secondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // 3. Vertical Divider Line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.5.dp)
                .offset { IntOffset(splitX - (1.25.dp.roundToPx()), 0) }
                .background(Color.White.copy(alpha = 0.9f))
        )

        // 4. Center Draggable Thumb Handle
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset { IntOffset(splitX - 20.dp.roundToPx(), (constraints.maxHeight / 2) - 20.dp.roundToPx()) }
                .clip(CircleShape)
                .background(colors.primary)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Drag slider",
                tint = colors.background,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
