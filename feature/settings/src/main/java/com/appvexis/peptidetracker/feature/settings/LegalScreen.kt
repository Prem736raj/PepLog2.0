package com.appvexis.peptidetracker.feature.settings

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Full-screen WebView for displaying legal documents
 * (Privacy Policy / Terms of Service) from local HTML assets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    type: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (type) {
        "privacy" -> "Privacy Policy"
        "terms" -> "Terms of Service"
        else -> "Legal"
    }

    val assetPath = when (type) {
        "privacy" -> "file:///android_asset/privacy_policy.html"
        "terms" -> "file:///android_asset/terms_of_service.html"
        else -> "file:///android_asset/privacy_policy.html"
    }
    val webViewBackground = PepLogTheme.colors.background.toArgb()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = PepLogTheme.colors.textPrimary,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PepLogTheme.colors.textPrimary,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PepLogTheme.colors.surface,
            ),
        )

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        @Suppress("SetJavaScriptEnabled")
                        javaScriptEnabled = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    setBackgroundColor(webViewBackground)
                    loadUrl(assetPath)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
