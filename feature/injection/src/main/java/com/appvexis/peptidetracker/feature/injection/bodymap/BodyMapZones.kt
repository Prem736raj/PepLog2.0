package com.appvexis.peptidetracker.feature.injection.bodymap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.appvexis.peptidetracker.core.model.InjectionSiteArea

/**
 * Hit zone definition for a tappable region on the body map Canvas.
 * Coordinates are expressed as fractions of the canvas width/height (0.0–1.0)
 * so the body map scales to any screen size.
 */
data class BodyMapZone(
    val area: InjectionSiteArea,
    val centerXFraction: Float,
    val centerYFraction: Float,
    val radiusFraction: Float = 0.045f
) {
    /**
     * Convert fractional coordinates to an absolute Rect for hit-testing.
     */
    fun toRect(canvasWidth: Float, canvasHeight: Float): Rect {
        val cx = centerXFraction * canvasWidth
        val cy = centerYFraction * canvasHeight
        val r = radiusFraction * canvasWidth
        return Rect(cx - r, cy - r, cx + r, cy + r)
    }

    fun toAbsoluteCenter(canvasWidth: Float, canvasHeight: Float): Offset {
        return Offset(centerXFraction * canvasWidth, centerYFraction * canvasHeight)
    }

    fun absoluteRadius(canvasWidth: Float): Float {
        return radiusFraction * canvasWidth
    }
}

/**
 * All tappable injection zones for the FRONT body view.
 * Positions are hand-tuned to match a proportional human silhouette.
 */
val FrontBodyZones = listOf(
    // Abdomen quadrants (SubQ)
    BodyMapZone(InjectionSiteArea.ABDOMEN_UPPER_LEFT, 0.42f, 0.44f, 0.055f),
    BodyMapZone(InjectionSiteArea.ABDOMEN_UPPER_RIGHT, 0.58f, 0.44f, 0.055f),
    BodyMapZone(InjectionSiteArea.ABDOMEN_LOWER_LEFT, 0.42f, 0.52f, 0.055f),
    BodyMapZone(InjectionSiteArea.ABDOMEN_LOWER_RIGHT, 0.58f, 0.52f, 0.055f),

    // Thighs - outer (SubQ)
    BodyMapZone(InjectionSiteArea.THIGH_OUTER_LEFT, 0.36f, 0.67f, 0.048f),
    BodyMapZone(InjectionSiteArea.THIGH_OUTER_RIGHT, 0.64f, 0.67f, 0.048f),

    // Thighs - inner (SubQ) - slightly inward
    BodyMapZone(InjectionSiteArea.THIGH_INNER_LEFT, 0.44f, 0.70f, 0.040f),
    BodyMapZone(InjectionSiteArea.THIGH_INNER_RIGHT, 0.56f, 0.70f, 0.040f),

    // Upper Arms (SubQ)
    BodyMapZone(InjectionSiteArea.UPPER_ARM_LEFT, 0.22f, 0.33f, 0.042f),
    BodyMapZone(InjectionSiteArea.UPPER_ARM_RIGHT, 0.78f, 0.33f, 0.042f),

    // Deltoids (IM)
    BodyMapZone(InjectionSiteArea.DELTOID_LEFT, 0.24f, 0.26f, 0.042f),
    BodyMapZone(InjectionSiteArea.DELTOID_RIGHT, 0.76f, 0.26f, 0.042f),

    // Vastus Lateralis (IM) - outer thigh, IM-specific positioning
    BodyMapZone(InjectionSiteArea.VASTUS_LATERALIS_LEFT, 0.34f, 0.63f, 0.042f),
    BodyMapZone(InjectionSiteArea.VASTUS_LATERALIS_RIGHT, 0.66f, 0.63f, 0.042f)
)

/**
 * All tappable injection zones for the BACK body view.
 */
val BackBodyZones = listOf(
    // Upper Buttocks (SubQ)
    BodyMapZone(InjectionSiteArea.UPPER_BUTTOCK_LEFT, 0.42f, 0.55f, 0.055f),
    BodyMapZone(InjectionSiteArea.UPPER_BUTTOCK_RIGHT, 0.58f, 0.55f, 0.055f),

    // Ventrogluteal (IM) - upper-outer quadrant of buttocks
    BodyMapZone(InjectionSiteArea.VENTROGLUTEAL_LEFT, 0.35f, 0.52f, 0.048f),
    BodyMapZone(InjectionSiteArea.VENTROGLUTEAL_RIGHT, 0.65f, 0.52f, 0.048f)
)
