package com.example.solodoc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.solodoc.ui.HomeScreen
import com.example.solodoc.ui.PdfViewerScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "home") {

                // 1. Home Screen
                composable("home") {
                    HomeScreen(onPdfClick = { uri ->
                        val encoded = URLEncoder.encode(uri.toString(), "UTF-8")
                        navController.navigate("viewer/$encoded")
                    })

                    // CHECK: Did we open from File Manager?
                    LaunchedEffect(Unit) {
                        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
                            val uri = intent.data
                            val encoded = URLEncoder.encode(uri.toString(), "UTF-8")
                            navController.navigate("viewer/$encoded")
                        }
                    }
                }

                // 2. Viewer Screen
                composable(
                    "viewer/{uri}",
                    arguments = listOf(navArgument("uri") { type = NavType.StringType })
                ) {
                    val uriString = it.arguments?.getString("uri")
                    if (uriString != null) {
                        PdfViewerScreen(android.net.Uri.parse(uriString))
                    }
                }
            }
        }
    }
}