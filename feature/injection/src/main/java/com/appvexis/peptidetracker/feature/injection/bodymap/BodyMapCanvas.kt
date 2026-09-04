package com.appvexis.peptidetracker.feature.injection.bodymap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.appvexis.peptidetracker.core.model.InjectionSiteArea
import com.appvexis.peptidetracker.feature.injection.model.ReadinessLevel
import com.appvexis.peptidetracker.feature.injection.model.SiteStatus

// Color constants for zone readiness
private val RecentColor = Color(0xFFB85E3C)       // Terracotta — used within 48h
private val HealingColor = Color(0xFF99681F)      // Ochre — 2-7 days
private val ReadyColor = Color(0xFF2F7656)        // Evergreen — ready
private val UnusedColor = Color(0xFF7D8A82)       // Slate — never used
private val SelectedRingColor = Color(0xFF1E625D) // Mineral teal — selected

/**
 * Interactive body map Canvas composable for the FRONT body view.
 * Draws a proportional human silhouette outline with color-coded injection zones.
 * Zones are tappable and change visual state based on SiteStatus.
 */
@Composable
fun BodyMapFrontCanvas(
    siteStatuses: Map<InjectionSiteArea, SiteStatus>,
    selectedSite: InjectionSiteArea?,
    onSiteClick: (InjectionSiteArea) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val textMeasurer = rememberTextMeasurer()
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hitZone = FrontBodyZones.find { zone ->
                        val rect = zone.toRect(canvasSize.width, canvasSize.height)
                        rect.contains(offset)
                    }
                    hitZone?.let { onSiteClick(it.area) }
                }
            }
    ) {
        canvasSize = size
        val w = size.width
        val h = size.height
        val outlineColor = if (isDark) Color(0xFF526159) else Color(0xFFC8CEC7)

        // Draw human body silhouette outline
        drawHumanFrontOutline(w, h, outlineColor)

        // Draw injection zones
        FrontBodyZones.forEach { zone ->
            val status = siteStatuses[zone.area]
            val isSelected = zone.area == selectedSite
            drawInjectionZone(
                zone = zone,
                status = status,
                isSelected = isSelected,
                textMeasurer = textMeasurer,
                isDark = isDark
            )
        }
    }
}

/**
 * Interactive body map Canvas composable for the BACK body view.
 */
@Composable
fun BodyMapBackCanvas(
    siteStatuses: Map<InjectionSiteArea, SiteStatus>,
    selectedSite: InjectionSiteArea?,
    onSiteClick: (InjectionSiteArea) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val textMeasurer = rememberTextMeasurer()
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hitZone = BackBodyZones.find { zone ->
                        val rect = zone.toRect(canvasSize.width, canvasSize.height)
                        rect.contains(offset)
                    }
                    hitZone?.let { onSiteClick(it.area) }
                }
            }
    ) {
        canvasSize = size
        val w = size.width
        val h = size.height
        val outlineColor = if (isDark) Color(0xFF526159) else Color(0xFFC8CEC7)

        // Draw human body back silhouette outline
        drawHumanBackOutline(w, h, outlineColor)

        // Draw injection zones
        BackBodyZones.forEach { zone ->
            val status = siteStatuses[zone.area]
            val isSelected = zone.area == selectedSite
            drawInjectionZone(
                zone = zone,
                status = status,
                isSelected = isSelected,
                textMeasurer = textMeasurer,
                isDark = isDark
            )
        }
    }
}

/**
 * Draws a single injection zone circle with color-coded status.
 */
private fun DrawScope.drawInjectionZone(
    zone: BodyMapZone,
    status: SiteStatus?,
    isSelected: Boolean,
    textMeasurer: TextMeasurer,
    isDark: Boolean
) {
    val center = zone.toAbsoluteCenter(size.width, size.height)
    val baseRadius = zone.absoluteRadius(size.width)
    val radius = baseRadius

    val readiness = status?.readinessLevel ?: ReadinessLevel.UNUSED
    val zoneColor = when (readiness) {
        ReadinessLevel.RECENT -> RecentColor
        ReadinessLevel.HEALING -> HealingColor
        ReadinessLevel.READY -> ReadyColor
        ReadinessLevel.UNUSED -> UnusedColor
    }

    // Fill circle with semi-transparent zone color
    drawCircle(
        color = zoneColor.copy(alpha = if (isSelected) 0.45f else 0.30f),
        center = center,
        radius = radius,
        style = Fill
    )

    // Border ring
    drawCircle(
        color = if (isSelected) SelectedRingColor else zoneColor.copy(alpha = 0.7f),
        center = center,
        radius = radius,
        style = Stroke(width = if (isSelected) 3f else 2f)
    )

    // Inner dot for used sites
    if (status?.lastUsedTimestamp != null) {
        drawCircle(
            color = zoneColor,
            center = center,
            radius = radius * 0.3f,
            style = Fill
        )
    }

    // Usage count label for sites used more than once
    val count = status?.usageCount ?: 0
    if (count > 0) {
        val labelStyle = TextStyle(
            color = if (isDark) Color(0xFFF0F3EE) else Color(0xFF17201C),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        val textResult = textMeasurer.measure(count.toString(), labelStyle)
        drawText(
            textResult,
            topLeft = Offset(
                center.x - textResult.size.width / 2f,
                center.y - textResult.size.height / 2f
            )
        )
    }
}

/**
 * Draw a simplified human body front silhouette using Path.
 * Uses smooth proportional curves for a premium, non-clinical look.
 */
private fun DrawScope.drawHumanFrontOutline(w: Float, h: Float, color: Color) {
    val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Head
    drawCircle(
        color = color,
        center = Offset(w * 0.5f, h * 0.08f),
        radius = w * 0.07f,
        style = stroke
    )

    // Neck
    val neck = Path().apply {
        moveTo(w * 0.46f, h * 0.14f)
        lineTo(w * 0.46f, h * 0.17f)
        moveTo(w * 0.54f, h * 0.14f)
        lineTo(w * 0.54f, h * 0.17f)
    }
    drawPath(neck, color, style = stroke)

    // Torso
    val torso = Path().apply {
        moveTo(w * 0.46f, h * 0.17f)
        // Left shoulder
        cubicTo(w * 0.30f, h * 0.17f, w * 0.22f, h * 0.22f, w * 0.20f, h * 0.26f)
        // Left arm
        lineTo(w * 0.16f, h * 0.42f)
        lineTo(w * 0.14f, h * 0.48f)
        // Move back up to join torso
        moveTo(w * 0.20f, h * 0.26f)
        // Left side of torso
        cubicTo(w * 0.26f, h * 0.30f, w * 0.28f, h * 0.40f, w * 0.30f, h * 0.56f)
        // Left hip
        cubicTo(w * 0.30f, h * 0.58f, w * 0.32f, h * 0.60f, w * 0.34f, h * 0.60f)
        // Left leg
        lineTo(w * 0.32f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.92f)
        lineTo(w * 0.34f, h * 0.92f)
        lineTo(w * 0.38f, h * 0.78f)
        lineTo(w * 0.42f, h * 0.60f)
        // Crotch
        cubicTo(w * 0.44f, h * 0.58f, w * 0.48f, h * 0.57f, w * 0.50f, h * 0.57f)
        cubicTo(w * 0.52f, h * 0.57f, w * 0.56f, h * 0.58f, w * 0.58f, h * 0.60f)
        // Right leg
        lineTo(w * 0.62f, h * 0.78f)
        lineTo(w * 0.66f, h * 0.92f)
        lineTo(w * 0.70f, h * 0.92f)
        lineTo(w * 0.68f, h * 0.78f)
        lineTo(w * 0.66f, h * 0.60f)
        // Right hip
        cubicTo(w * 0.68f, h * 0.60f, w * 0.70f, h * 0.58f, w * 0.70f, h * 0.56f)
        // Right side of torso
        cubicTo(w * 0.72f, h * 0.40f, w * 0.74f, h * 0.30f, w * 0.80f, h * 0.26f)
        // Right arm
        moveTo(w * 0.80f, h * 0.26f)
        lineTo(w * 0.84f, h * 0.42f)
        lineTo(w * 0.86f, h * 0.48f)
        // Right shoulder
        moveTo(w * 0.80f, h * 0.26f)
        cubicTo(w * 0.78f, h * 0.22f, w * 0.70f, h * 0.17f, w * 0.54f, h * 0.17f)
    }
    drawPath(torso, color, style = stroke)
}

/**
 * Draw a simplified human body back silhouette using Path.
 */
private fun DrawScope.drawHumanBackOutline(w: Float, h: Float, color: Color) {
    val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Head
    drawCircle(
        color = color,
        center = Offset(w * 0.5f, h * 0.08f),
        radius = w * 0.07f,
        style = stroke
    )

    // Neck
    val neck = Path().apply {
        moveTo(w * 0.46f, h * 0.14f)
        lineTo(w * 0.46f, h * 0.17f)
        moveTo(w * 0.54f, h * 0.14f)
        lineTo(w * 0.54f, h * 0.17f)
    }
    drawPath(neck, color, style = stroke)

    // Back torso (mirrored front with spine indicator)
    val torso = Path().apply {
        moveTo(w * 0.46f, h * 0.17f)
        cubicTo(w * 0.30f, h * 0.17f, w * 0.22f, h * 0.22f, w * 0.20f, h * 0.26f)
        lineTo(w * 0.16f, h * 0.42f)
        lineTo(w * 0.14f, h * 0.48f)
        moveTo(w * 0.20f, h * 0.26f)
        cubicTo(w * 0.26f, h * 0.30f, w * 0.28f, h * 0.40f, w * 0.30f, h * 0.56f)
        cubicTo(w * 0.30f, h * 0.58f, w * 0.32f, h * 0.60f, w * 0.34f, h * 0.60f)
        lineTo(w * 0.32f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.92f)
        lineTo(w * 0.34f, h * 0.92f)
        lineTo(w * 0.38f, h * 0.78f)
        lineTo(w * 0.42f, h * 0.60f)
        cubicTo(w * 0.44f, h * 0.58f, w * 0.48f, h * 0.57f, w * 0.50f, h * 0.57f)
        cubicTo(w * 0.52f, h * 0.57f, w * 0.56f, h * 0.58f, w * 0.58f, h * 0.60f)
        lineTo(w * 0.62f, h * 0.78f)
        lineTo(w * 0.66f, h * 0.92f)
        lineTo(w * 0.70f, h * 0.92f)
        lineTo(w * 0.68f, h * 0.78f)
        lineTo(w * 0.66f, h * 0.60f)
        cubicTo(w * 0.68f, h * 0.60f, w * 0.70f, h * 0.58f, w * 0.70f, h * 0.56f)
        cubicTo(w * 0.72f, h * 0.40f, w * 0.74f, h * 0.30f, w * 0.80f, h * 0.26f)
        moveTo(w * 0.80f, h * 0.26f)
        lineTo(w * 0.84f, h * 0.42f)
        lineTo(w * 0.86f, h * 0.48f)
        moveTo(w * 0.80f, h * 0.26f)
        cubicTo(w * 0.78f, h * 0.22f, w * 0.70f, h * 0.17f, w * 0.54f, h * 0.17f)
    }
    drawPath(torso, color, style = stroke)

    // Spine line (distinguishes back view from front)
    val spine = Path().apply {
        moveTo(w * 0.50f, h * 0.17f)
        lineTo(w * 0.50f, h * 0.52f)
    }
    drawPath(spine, color.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
}
