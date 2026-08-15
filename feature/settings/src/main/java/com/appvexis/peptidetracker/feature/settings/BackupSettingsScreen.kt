package com.appvexis.peptidetracker.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvexis.peptidetracker.core.backup.model.BackupStatus
import com.appvexis.peptidetracker.core.backup.model.DriveBackupFile
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup & Export settings screen.
 * Allows users to:
 * - Back up to Google Drive
 * - Restore from Google Drive
 * - Export data as CSV or JSON
 * - Toggle auto-backup
 */
@Composable
fun BackupSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val driveBackups by viewModel.driveBackups.collectAsState()
    val isLoadingBackups by viewModel.isLoadingBackups.collectAsState()
    val restoreConfirmation by viewModel.showRestoreConfirmation.collectAsState()

    // Load Drive backups when account is connected
    LaunchedEffect(preferences.googleAccountEmail) {
        if (!preferences.googleAccountEmail.isNullOrBlank()) {
            viewModel.loadDriveBackups()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PepLogTheme.colors.background)
    ) {
        // Top Bar
        BackupTopBar(onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PepLogTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(PepLogTheme.spacing.medium)
        ) {
            // Status Banner
            item {
                BackupStatusBanner(
                    status = backupStatus,
                    onDismiss = { viewModel.resetStatus() }
                )
            }

            // Cloud Backup Section
            item {
                SectionHeader(title = "Cloud Backup")
            }

            item {
                CloudBackupCard(
                    lastBackupTime = preferences.lastBackupTime,
                    lastBackupSize = preferences.lastBackupSize,
                    accountEmail = preferences.googleAccountEmail,
                    isBackingUp = backupStatus is BackupStatus.InProgress,
                    onBackupNow = { viewModel.backupNow() }
                )
            }

            // Auto-Backup Toggle
            item {
                AutoBackupCard(
                    isEnabled = preferences.isAutoBackupEnabled,
                    onToggle = { viewModel.setAutoBackup(it) }
                )
            }

            // Drive Backups List
            if (!preferences.googleAccountEmail.isNullOrBlank()) {
                item {
                    SectionHeader(title = "Available Backups")
                }

                if (isLoadingBackups) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PepLogTheme.colors.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else if (driveBackups.isEmpty()) {
                    item {
                        Text(
                            text = "No backups found on Google Drive.",
                            color = PepLogTheme.colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(driveBackups, key = { it.id }) { backup ->
                        DriveBackupItem(
                            backup = backup,
                            onRestore = { viewModel.requestRestore(backup) }
                        )
                    }
                }
            }

            // Export Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Local Export")
            }

            item {
                ExportOptionCard(
                    icon = Icons.Default.TableChart,
                    title = "Export as CSV",
                    description = "Separate CSV files for dose logs, protocols, biomarkers, inventory, and more.",
                    onClick = { viewModel.exportCsv() }
                )
            }

            item {
                ExportOptionCard(
                    icon = Icons.Default.Description,
                    title = "Export as JSON",
                    description = "Full database export as a single JSON file — ideal for complete data backup.",
                    onClick = { viewModel.exportJson() }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Restore Confirmation Dialog
    restoreConfirmation?.let { backup ->
        RestoreConfirmationDialog(
            backup = backup,
            onConfirm = { viewModel.confirmRestore(backup) },
            onDismiss = { viewModel.dismissRestoreConfirmation() }
        )
    }
}

@Composable
private fun BackupTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PepLogTheme.colors.surface)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PepLogTheme.colors.textPrimary
            )
        }
        Text(
            text = "Backup & Export",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = PepLogTheme.colors.textPrimary
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = PepLogTheme.colors.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun BackupStatusBanner(
    status: BackupStatus,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = status !is BackupStatus.Idle,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PepLogCard(
            modifier = Modifier.fillMaxWidth(),
            isGlassmorphic = false
        ) {
            Column(modifier = Modifier.padding(PepLogTheme.spacing.medium)) {
                when (status) {
                    is BackupStatus.InProgress -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PepLogTheme.colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = status.message,
                                color = PepLogTheme.colors.textPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { status.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PepLogTheme.colors.primary,
                            trackColor = PepLogTheme.colors.primary.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                    is BackupStatus.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = PepLogTheme.colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Backup successful!",
                                    color = PepLogTheme.colors.success,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = formatFileSize(status.sizeBytes),
                                    color = PepLogTheme.colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            TextButton(onClick = onDismiss) {
                                Text("OK", color = PepLogTheme.colors.primary)
                            }
                        }
                    }
                    is BackupStatus.Error -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = PepLogTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Backup failed",
                                    color = PepLogTheme.colors.accent,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = status.message,
                                    color = PepLogTheme.colors.textSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TextButton(onClick = onDismiss) {
                                Text("OK", color = PepLogTheme.colors.primary)
                            }
                        }
                    }
                    BackupStatus.Idle -> { /* Hidden */ }
                }
            }
        }
    }
}

@Composable
private fun CloudBackupCard(
    lastBackupTime: Long?,
    lastBackupSize: Long?,
    accountEmail: String?,
    isBackingUp: Boolean,
    onBackupNow: () -> Unit
) {
    PepLogCard(
        modifier = Modifier.fillMaxWidth(),
        isGlassmorphic = false
    ) {
        Column(modifier = Modifier.padding(PepLogTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PepLogTheme.colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = PepLogTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google Drive Backup",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PepLogTheme.colors.textPrimary
                    )
                    if (accountEmail != null) {
                        Text(
                            text = accountEmail,
                            color = PepLogTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last backup info
            if (lastBackupTime != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PepLogTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Last backup: ${formatDate(lastBackupTime)}",
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 13.sp
                    )
                    if (lastBackupSize != null) {
                        Text(
                            text = " · ${formatFileSize(lastBackupSize)}",
                            color = PepLogTheme.colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Backup Now button
            Button(
                onClick = onBackupNow,
                enabled = !isBackingUp && !accountEmail.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PepLogTheme.colors.primary,
                    contentColor = PepLogTheme.colors.background
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isBackingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PepLogTheme.colors.background,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backing up...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Now")
                }
            }

            if (accountEmail.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in with Google from App Settings to enable cloud backup.",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AutoBackupCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PepLogCard(
        modifier = Modifier.fillMaxWidth(),
        isGlassmorphic = false
    ) {
        Row(
            modifier = Modifier.padding(PepLogTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PepLogTheme.colors.secondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PepLogTheme.colors.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto-Backup",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Text(
                    text = "Daily automatic backup to Google Drive",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PepLogTheme.colors.primary,
                    checkedTrackColor = PepLogTheme.colors.primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun DriveBackupItem(
    backup: DriveBackupFile,
    onRestore: () -> Unit
) {
    PepLogCard(
        modifier = Modifier.fillMaxWidth(),
        isGlassmorphic = false
    ) {
        Row(
            modifier = Modifier.padding(PepLogTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = PepLogTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = PepLogTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = formatDate(backup.createdTime),
                        color = PepLogTheme.colors.textSecondary,
                        fontSize = 12.sp
                    )
                    if (backup.sizeBytes > 0) {
                        Text(
                            text = " · ${formatFileSize(backup.sizeBytes)}",
                            color = PepLogTheme.colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            TextButton(onClick = onRestore) {
                Text("Restore", color = PepLogTheme.colors.primary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    PepLogCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        isGlassmorphic = false
    ) {
        Row(
            modifier = Modifier.padding(PepLogTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PepLogTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PepLogTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PepLogTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                tint = PepLogTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RestoreConfirmationDialog(
    backup: DriveBackupFile,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PepLogTheme.colors.surface,
        title = {
            Text(
                text = "Restore Backup?",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                color = PepLogTheme.colors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "This will replace your current data with the backup from:",
                    color = PepLogTheme.colors.textSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDate(backup.createdTime),
                    color = PepLogTheme.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚠️ This action cannot be undone. Make sure to backup your current data first.",
                    color = PepLogTheme.colors.accent,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PepLogTheme.colors.accent
                )
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PepLogTheme.colors.textSecondary)
            }
        }
    )
}

// ---- Utility formatters ---- //

private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US)

private fun formatDate(millis: Long): String {
    return if (millis > 0) dateFormat.format(Date(millis)) else "Never"
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
