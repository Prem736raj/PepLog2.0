package com.appvexis.peptidetracker.core.common.security

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper around Google Play Integrity API.
 *
 * Requests an integrity token on app launch and exposes device trust
 * status. Because PepLog has no backend, we cannot fully verify the
 * token server-side — we only check that the call *succeeds* which
 * already filters out obviously tampered environments.
 */
@Singleton
class AppIntegrityChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityConfig: SecurityConfig,
) {
    private val _isDeviceTrusted = MutableStateFlow(true) // optimistic default
    val isDeviceTrusted: StateFlow<Boolean> = _isDeviceTrusted.asStateFlow()

    private val _lastCheckTimestamp = MutableStateFlow(0L)
    val lastCheckTimestamp: StateFlow<Long> = _lastCheckTimestamp.asStateFlow()

    /**
     * Requests an integrity token. A successful response is only a local
     * availability signal until a trusted backend decodes and verifies the
     * token; this app has no backend and therefore cannot make an authenticity
     * or anti-tamper guarantee from this check alone.
     *
     * Note: Without a backend to decode the token, we treat
     * "request succeeded" as a positive signal. A failure (e.g. on
     * rooted devices, emulators, or tampered builds) marks the
     * device as untrusted.
     */
    fun checkIntegrity() {
        try {
            val integrityManager = IntegrityManagerFactory.create(context)

            val requestBuilder = IntegrityTokenRequest.builder()
                .setNonce(generateNonce())

            // Bind to Google Cloud project when configured
            val projectNumber = securityConfig.cloudProjectNumber
            if (projectNumber > 0L) {
                requestBuilder.setCloudProjectNumber(projectNumber)
            }

            val request = requestBuilder.build()

            integrityManager.requestIntegrityToken(request)
                .addOnSuccessListener { response ->
                    // Token received — Play Integrity is available. The token
                    // is deliberately not treated as verified without a backend.
                    val token = response.token()
                    Timber.d("Play Integrity check passed (token length: ${token.length})")
                    _isDeviceTrusted.value = true
                    _lastCheckTimestamp.value = System.currentTimeMillis()
                }
                .addOnFailureListener { exception ->
                    Timber.w(exception, "Play Integrity check failed")
                    _isDeviceTrusted.value = false
                    _lastCheckTimestamp.value = System.currentTimeMillis()
                }
        } catch (e: Exception) {
            Timber.w(e, "Play Integrity not available")
            // Don't block the user on environments where Play Integrity
            // is unavailable (e.g. Huawei, Amazon Fire). Keep trusted.
            _isDeviceTrusted.value = true
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(24)
        java.security.SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
}
