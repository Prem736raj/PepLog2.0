package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

@Composable
fun PepLogDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = PepLogTheme.colors
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismissRequest() }) {
                Text(confirmButtonText, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = dismissButtonText?.let { label ->
            {
                TextButton(onClick = { onDismiss?.invoke(); onDismissRequest() }) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                }
            }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) },
        icon = icon,
        shape = MaterialTheme.shapes.large,
        containerColor = colors.surface,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        modifier = modifier,
    )
}
