package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/**
 * Premium text field v2 with focus glow and refined styling.
 */
@Composable
fun PepLogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 10
) {
    val colors = PepLogTheme.colors
    val shape = RoundedCornerShape(14.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = placeholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage, style = MaterialTheme.typography.bodySmall, color = colors.accent) }
        } else null,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            disabledTextColor = colors.textSecondary,
            focusedContainerColor = if (colors.isDark) Color(0xFF0F1729) else colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surfaceHigh,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
            disabledBorderColor = if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
            errorBorderColor = colors.accent,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.textSecondary,
            errorLabelColor = colors.accent,
            cursorColor = colors.primary,
            errorCursorColor = colors.accent
        )
    )
}
