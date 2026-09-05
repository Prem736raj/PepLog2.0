package com.appvexis.peptidetracker.feature.progress.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogButtonVariant
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogChip
import com.appvexis.peptidetracker.core.ui.components.PepLogTextField
import com.appvexis.peptidetracker.core.ui.theme.OutfitFontFamily
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.feature.progress.model.PhotoCategory

/**
 * Dialog for capturing or uploading a new progress photo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProgressPhotoDialog(
    protocols: List<Protocol>,
    onDismiss: () -> Unit,
    onConfirm: (photoUri: String, category: String, protocolId: String?, notes: String?) -> Unit
) {
    val colors = PepLogTheme.colors

    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(PhotoCategory.FRONT) }
    var selectedProtocolId by remember { mutableStateOf<String?>(protocols.firstOrNull()?.id) }
    var notesInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { selectedPhotoUri = it.toString() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PepLogCard(
            isGlassmorphic = true,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Progress Photo",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = colors.textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Photo Preview or Picker Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceHigh)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedPhotoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap to choose from Gallery",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = colors.primary
                            )
                            Text(
                                text = "JPEG or PNG",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Category Selection
                Text(
                    text = "Pose / Angle Category",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(PhotoCategory.FRONT, PhotoCategory.SIDE, PhotoCategory.BACK, PhotoCategory.DETAIL).forEach { cat ->
                        PepLogChip(
                            text = cat.displayName,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                // Associated Protocol
                if (protocols.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))
                    Text(
                        text = "Associated protocol",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PepLogChip(
                            text = "None (General)",
                            selected = selectedProtocolId == null,
                            onClick = { selectedProtocolId = null }
                        )
                        protocols.forEach { proto ->
                            PepLogChip(
                                text = proto.name,
                                selected = selectedProtocolId == proto.id,
                                onClick = { selectedProtocolId = proto.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.medium))

                // Notes
                PepLogTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = "Notes (Optional)",
                    placeholder = "e.g. Fasted morning check-in, end of week 4"
                )

                Spacer(modifier = Modifier.height(PepLogTheme.spacing.large))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PepLogButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        variant = PepLogButtonVariant.Ghost,
                        modifier = Modifier.weight(1f)
                    )

                    PepLogButton(
                        text = "Save Photo",
                        onClick = {
                            selectedPhotoUri?.let { uri ->
                                onConfirm(uri, selectedCategory.name, selectedProtocolId, notesInput.takeIf { it.isNotBlank() })
                            }
                        },
                        enabled = selectedPhotoUri != null,
                        variant = PepLogButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
