package com.appvexis.peptidetracker.core.model

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route for the Dashboard screen.
 */
@Serializable
object DashboardRoute

/**
 * Type-safe navigation route for the Protocols screen.
 */
@Serializable
object ProtocolsRoute

/**
 * Type-safe navigation route for the Insights/Reports screen.
 */
@Serializable
object InsightsRoute

/**
 * Type-safe navigation route for the More/Settings screen.
 */
@Serializable
object MoreRoute

/**
 * Type-safe navigation route for the Onboarding screen.
 */
@Serializable
object OnboardingRoute

/**
 * Type-safe navigation route for the Peptide Encyclopedia list screen.
 */
@Serializable
object EncyclopediaRoute

/**
 * Type-safe navigation route for the Peptide Detail screen.
 */
@Serializable
data class PeptideDetailRoute(val peptideId: String)

/**
 * Type-safe navigation route for the Reconstitution Calculator screen.
 */
@Serializable
object CalculatorRoute

// Protocol Routes
@Serializable
data class ProtocolDetailRoute(val protocolId: String)

@Serializable
object CreateProtocolRoute

/** A compact first-run path for creating one daily reminder. */
@Serializable
object QuickStartRoute

@Serializable
data class AddCompoundRoute(val protocolId: String)

// Dose Logging Routes
@Serializable
object DailyLogRoute

@Serializable
object DoseHistoryRoute

// Injection Site Tracker Routes
@Serializable
object InjectionTrackerRoute

@Serializable
data class InjectionSiteDetailRoute(val bodyArea: String)

// Inventory Management Routes
@Serializable
object InventoryRoute

// Progress & Biomarker Tracking Routes
@Serializable
object ProgressRoute

@Serializable
data class PhotoComparisonRoute(
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null
)

// PK Half-Life Visualizer Route
@Serializable
object PKVisualizerRoute

// Health Connect Integration Route
@Serializable
object HealthConnectRoute

// Backup & Export Settings Route
@Serializable
object BackupSettingsRoute

// Splash Screen Route
@Serializable
object SplashRoute

// Legal (Privacy Policy / Terms) Route
@Serializable
data class LegalRoute(val type: String) // "privacy" or "terms"
