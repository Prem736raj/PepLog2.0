package com.appvexis.peptidetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme

/** A compact vial-and-record mark used consistently instead of generic science imagery. */
object PepLogIcons {
    val DoseLog: ImageVector by lazy {
        materialIcon(name = "PepLog.DoseLog") {
            materialPath {
                moveTo(8.0f, 2.5f)
                horizontalLineTo(16.0f)
                verticalLineTo(5.5f)
                horizontalLineTo(15.0f)
                verticalLineTo(8.0f)
                lineTo(18.0f, 18.5f)
                curveTo(18.5f, 20.2f, 17.2f, 21.5f, 15.5f, 21.5f)
                horizontalLineTo(8.5f)
                curveTo(6.8f, 21.5f, 5.5f, 20.2f, 6.0f, 18.5f)
                lineTo(9.0f, 8.0f)
                verticalLineTo(5.5f)
                horizontalLineTo(8.0f)
                close()
                moveTo(8.6f, 16.0f)
                horizontalLineTo(15.4f)
                lineTo(16.0f, 18.2f)
                curveTo(16.2f, 18.9f, 15.7f, 19.5f, 15.0f, 19.5f)
                horizontalLineTo(9.0f)
                curveTo(8.3f, 19.5f, 7.8f, 18.9f, 8.0f, 18.2f)
                close()
                moveTo(11.0f, 10.5f)
                horizontalLineTo(13.0f)
                verticalLineTo(14.5f)
                horizontalLineTo(11.0f)
                close()
            }
        }
    }

    val Trend: ImageVector by lazy {
        materialIcon(name = "PepLog.Trend") {
            materialPath {
                moveTo(4.0f, 19.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(21.0f)
                horizontalLineTo(2.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(4.0f)
                close()
                moveTo(6.0f, 16.0f)
                lineTo(10.0f, 12.0f)
                lineTo(13.0f, 15.0f)
                lineTo(19.0f, 8.0f)
                lineTo(20.5f, 9.3f)
                lineTo(13.1f, 18.0f)
                lineTo(10.0f, 14.9f)
                lineTo(7.4f, 17.5f)
                close()
            }
        }
    }

    val Photo: ImageVector by lazy {
        materialIcon(name = "PepLog.Photo") {
            materialPath {
                moveTo(4.0f, 5.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(4.0f)
                close()
                moveTo(6.0f, 17.0f)
                lineTo(10.0f, 12.0f)
                lineTo(13.0f, 15.5f)
                lineTo(15.0f, 13.0f)
                lineTo(18.0f, 17.0f)
                close()
                moveTo(8.0f, 8.0f)
                horizontalLineTo(10.5f)
                verticalLineTo(10.5f)
                horizontalLineTo(8.0f)
                close()
            }
        }
    }

    val Eye: ImageVector by lazy {
        materialIcon(name = "PepLog.Eye") {
            materialPath {
                moveTo(12.0f, 5.0f)
                curveTo(7.0f, 5.0f, 3.3f, 8.1f, 2.0f, 12.0f)
                curveTo(3.3f, 15.9f, 7.0f, 19.0f, 12.0f, 19.0f)
                curveTo(17.0f, 19.0f, 20.7f, 15.9f, 22.0f, 12.0f)
                curveTo(20.7f, 8.1f, 17.0f, 5.0f, 12.0f, 5.0f)
                close()
                moveTo(12.0f, 8.0f)
                curveTo(14.2f, 8.0f, 16.0f, 9.8f, 16.0f, 12.0f)
                curveTo(16.0f, 14.2f, 14.2f, 16.0f, 12.0f, 16.0f)
                curveTo(9.8f, 16.0f, 8.0f, 14.2f, 8.0f, 12.0f)
                curveTo(8.0f, 9.8f, 9.8f, 8.0f, 12.0f, 8.0f)
                close()
            }
        }
    }

    val Swap: ImageVector by lazy {
        materialIcon(name = "PepLog.Swap") {
            materialPath {
                moveTo(7.0f, 7.0f)
                horizontalLineTo(18.0f)
                lineTo(15.0f, 4.0f)
                lineTo(16.4f, 2.6f)
                lineTo(21.8f, 8.0f)
                lineTo(16.4f, 13.4f)
                lineTo(15.0f, 12.0f)
                lineTo(18.0f, 9.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(17.0f, 17.0f)
                horizontalLineTo(6.0f)
                lineTo(9.0f, 20.0f)
                lineTo(7.6f, 21.4f)
                lineTo(2.2f, 16.0f)
                lineTo(7.6f, 10.6f)
                lineTo(9.0f, 12.0f)
                lineTo(6.0f, 15.0f)
                horizontalLineTo(17.0f)
                close()
            }
        }
    }
}

@Composable
fun PepLogBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    contentDescription: String? = null,
) {
    val colors = PepLogTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .background(colors.primary, androidx.compose.material3.MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PepLogIcons.DoseLog,
            contentDescription = contentDescription,
            tint = if (colors.isDark) colors.background else androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.size(size * 0.52f),
        )
    }
}
