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
 * @property cloudProjectNumber Google Cloud project number for Play Integrity API.
 *                               Zero disables cloud project binding.
 */
data class SecurityConfig(
    val releaseCertHash: String = "",
    val cloudProjectNumber: Long = 0L,
)
