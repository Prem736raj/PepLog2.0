package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Premium dialog v2 with refined shape and color tokens.
 */
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
    icon: (@Composable () -> Unit)? = null
) {
    val colors = PepLogTheme.colors
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismissRequest()
            }) {
                Text(
                    text = confirmButtonText,
                    color = colors.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = dismissButtonText?.let {
            {
                TextButton(onClick = {
                    onDismiss?.invoke()
                    onDismissRequest()
                }) {
                    Text(
                        text = it,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        icon = icon,
        shape = RoundedCornerShape(20.dp),
        containerColor = colors.surfaceHigh,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
        modifier = modifier
    )
}
