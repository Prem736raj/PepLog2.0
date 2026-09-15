package com.example.solodoc.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solodoc.data.DocFile
import com.example.solodoc.data.DocType
import com.example.solodoc.data.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FileRepository
) : ViewModel() {
    var allFiles by mutableStateOf<List<DocFile>>(emptyList())
    var filteredFiles by mutableStateOf<List<DocFile>>(emptyList())
    var selectedTab by mutableStateOf(0) // 0=All, 1=PDF, 2=PPT

    fun loadFiles() {
        viewModelScope.launch {
            allFiles = repository.getAllDocuments()
            filterFiles()
        }
    }

    fun filterFiles() {
        filteredFiles = when (selectedTab) {
            1 -> allFiles.filter { it.type == DocType.PDF }
            2 -> allFiles.filter { it.type == DocType.PPT }
            else -> allFiles
        }
    }

    fun updateTab(index: Int) {
        selectedTab = index
        filterFiles()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPdfClick: (Uri) -> Unit
) {
    val context = LocalContext.current

    // Permission Handling
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it) viewModel.loadFiles()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("SoloDoc Reader") })
                TabRow(selectedTabIndex = viewModel.selectedTab) {
                    listOf("All", "PDF", "PPT").forEachIndexed { index, title ->
                        Tab(
                            selected = viewModel.selectedTab == index,
                            onClick = { viewModel.updateTab(index) },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(viewModel.filteredFiles) { file ->
                FileItem(file = file, onClick = {
                    if (file.type == DocType.PDF) {
                        onPdfClick(file.uri)
                    } else {
                        // Open PPT in external app
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(file.uri, "application/vnd.ms-powerpoint")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback if no app found
                        }
                    }
                })
            }
        }
    }
}

@Composable
fun FileItem(file: DocFile, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(file.name, maxLines = 1, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(if(file.type == DocType.PDF) "PDF Document" else "PowerPoint Slide") },
        leadingContent = {
            Icon(
                if (file.type == DocType.PDF) Icons.Default.Description else Icons.Default.Slideshow,
                contentDescription = null,
                tint = if (file.type == DocType.PDF) Color.Red else Color(0xFFFF5722)
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}