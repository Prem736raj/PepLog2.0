package com.appvexis.peptidetracker.core.common.security

/**
 * Configuration values for runtime security checks.
 *
 * Provided by the :app module via Hilt DI, allowing the library module
 * to remain agnostic of BuildConfig while still receiving build-time
 * security parameters.
 *
 * @property releaseCertHash SHA-256 hash of the release signing certificate
 *                           (colon-separated hex, e.g. "AA:BB:CC:...").
 *                           Empty string disables signature verification.
 */
data class SecurityConfig(
    val releaseCertHash: String = "",
)
