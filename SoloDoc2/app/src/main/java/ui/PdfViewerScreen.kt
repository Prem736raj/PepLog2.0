package com.example.solodoc.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PdfViewerScreen(fileUri: Uri) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    
    // Setup Renderer
    LaunchedEffect(fileUri) {
        withContext(Dispatchers.IO) {
            try {
                val pfd = context.contentResolver.openFileDescriptor(fileUri, "r")
                if (pfd != null) {
                    val r = PdfRenderer(pfd)
                    renderer = r
                    pageCount = r.pageCount
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose { renderer?.close() }
    }

    if (renderer != null) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE))
        ) {
            items(pageCount) { index ->
                PdfPage(renderer!!, index)
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PdfPage(renderer: PdfRenderer, index: Int) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels

    LaunchedEffect(index) {
        withContext(Dispatchers.IO) {
            synchronized(renderer) {
                try {
                    val page = renderer.openPage(index)
                    val aspectRatio = page.width.toFloat() / page.height.toFloat()
                    val h = (screenWidth / aspectRatio).toInt()
                    val bmp = Bitmap.createBitmap(screenWidth, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmap = bmp
                } catch (e: Exception) { }
            }
        }
    }

    if (bitmap != null) {
        // Zoom Logic
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val state = rememberTransformableState { zoom, pan, _ ->
            scale = (scale * zoom).coerceIn(1f, 3f)
            if(scale > 1f) offset += pan else offset = Offset.Zero
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RectangleShape)
                .transformable(state)
        ) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }
    } else {
        Box(Modifier.fillMaxWidth().height(300.dp).background(Color.White))
    }
}