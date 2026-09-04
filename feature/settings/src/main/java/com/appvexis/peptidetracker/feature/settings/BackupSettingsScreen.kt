package com.appvexis.peptidetracker.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.FileProvider
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import com.appvexis.peptidetracker.core.ui.components.PepLogBrandMark
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Local, portable data exports. There is no hidden cloud-sync promise in this build. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsState()
    val status by viewModel.backupStatus.collectAsState()
    val shareRequest by viewModel.shareRequest.collectAsState()
    val context = LocalContext.current
    val colors = PepLogTheme.colors

    LaunchedEffect(shareRequest) {
        val request = shareRequest ?: return@LaunchedEffect
        try {
            val uris = request.files.map { file ->
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
            val shareIntent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = request.mimeType
                    putExtra(Intent.EXTRA_STREAM, uris.single())
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = request.mimeType
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                }
            }.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(shareIntent, "Share PepLog export"))
        } catch (_: ActivityNotFoundException) {
            // The export remains safely in the app cache; no receiver is installed.
        } catch (_: IllegalArgumentException) {
            // A malformed or unavailable FileProvider URI should not crash the settings screen.
        } finally {
            viewModel.clearShareRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export your data") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
            )
        },
        containerColor = colors.background,
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                PepLogCard(isGlassmorphic = true, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PepLogBrandMark(size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You control the copy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "PepLog keeps records on this device. An export is created only when you choose one, then Android lets you choose where to share it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }

            if (status !is BackupStatus.Idle) {
                item { ExportStatusCard(status = status, onDismiss = viewModel::resetStatus) }
            }

            item {
                Text(
                    text = "Create an export",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                ExportOptionCard(
                    icon = Icons.Default.TableChart,
                    iconTint = colors.primary,
                    title = "CSV files",
                    description = "Separate files for logs, protocols, biomarkers, inventory, and related records. Best for a spreadsheet.",
                    onClick = viewModel::exportCsv,
                )
            }
            item {
                ExportOptionCard(
                    icon = Icons.Default.Description,
                    iconTint = colors.secondary,
                    title = "Full JSON archive",
                    description = "One complete portable snapshot of your records. Best for keeping a personal archive.",
                    onClick = viewModel::exportJson,
                )
            }

            preferences.lastExportTime?.let { lastExportTime ->
                item {
                    PepLogCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Last export",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = buildString {
                                append(formatDate(lastExportTime))
                                preferences.lastExportSize?.let { append(" · ${formatFileSize(it)}") }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                PepLogCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Before you share",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Exports can contain health-related notes and timestamps. Store them only in a place you trust, and delete old copies you no longer need.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportStatusCard(status: BackupStatus, onDismiss: () -> Unit) {
    val colors = PepLogTheme.colors
    PepLogCard(isGlassmorphic = true, modifier = Modifier.fillMaxWidth()) {
        when (status) {
            is BackupStatus.InProgress -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.primary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(text = status.message, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { status.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primary,
                    trackColor = colors.surfaceDim,
                )
            }

            is BackupStatus.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.success)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export ready", style = MaterialTheme.typography.titleSmall, color = colors.success)
                        Text(formatFileSize(status.sizeBytes), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }

            is BackupStatus.Error -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.accent)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export could not be created", style = MaterialTheme.typography.titleSmall, color = colors.accent)
                        Text(status.message, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }

            BackupStatus.Idle -> Unit
        }
    }
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val colors = PepLogTheme.colors
    PepLogCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
    }
}

private val exportDateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

private fun formatDate(timestamp: Long): String = exportDateFormat.format(Date(timestamp))

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
}
