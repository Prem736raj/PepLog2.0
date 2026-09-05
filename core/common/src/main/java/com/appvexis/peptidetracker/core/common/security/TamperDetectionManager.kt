package com.appvexis.peptidetracker.core.common.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runtime tampering detection.
 *
 * Checks for common signs of a modified or hostile environment:
 * - Root / superuser access
 * - Attached debugger
 * - Xposed / LSPosed framework
 * - Known patching tool packages (LuckyPatcher, etc.)
 * - App signature mismatch (repackaged APK)
 *
 * Results are combined into [isTampered] for diagnostic logging only.
 */
@Singleton
class TamperDetectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityConfig: SecurityConfig,
) {
    private val _isTampered = MutableStateFlow(false)
    val isTampered: StateFlow<Boolean> = _isTampered.asStateFlow()

    private val _detectionDetails = MutableStateFlow<List<String>>(emptyList())
    val detectionDetails: StateFlow<List<String>> = _detectionDetails.asStateFlow()

    /**
     * Run all checks and update state. Should be called once during
     * Application.onCreate() and optionally on resume.
     */
    fun runChecks() {
        val findings = mutableListOf<String>()

        if (isRooted()) findings.add("ROOT_DETECTED")
        if (isDebuggerAttached()) findings.add("DEBUGGER_ATTACHED")
        if (isDebuggableBuild()) findings.add("DEBUGGABLE_BUILD")
        if (isXposedPresent()) findings.add("XPOSED_FRAMEWORK")
        if (hasPatchingTools()) findings.add("PATCHING_TOOLS")
        if (isSignatureMismatch()) findings.add("SIGNATURE_MISMATCH")

        _detectionDetails.value = findings
        _isTampered.value = findings.isNotEmpty()

        if (findings.isNotEmpty()) {
            Timber.w("Tamper detection findings: $findings")
        } else {
            Timber.d("Tamper detection: clean")
        }
    }

    // ---------------------------------------------------------------
    // Individual checks
    // ---------------------------------------------------------------

    /**
     * Checks for common root indicators: su binary, Magisk, superuser apps.
     */
    private fun isRooted(): Boolean {
        // Check for su binary in common paths
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
        )
        if (suPaths.any { File(it).exists() }) return true

        // Check for Magisk
        if (File("/sbin/.magisk").exists() || File("/data/adb/magisk").exists()) return true

        // Check for superuser-related packages
        val rootPackages = listOf(
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.topjohnwu.magisk",
        )
        return rootPackages.any { isPackageInstalled(it) }
    }

    /**
     * Detects if a Java debugger is currently attached.
     */
    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Checks if the APK was built with the debuggable flag.
     * This should be false in release builds.
     */
    private fun isDebuggableBuild(): Boolean {
        return try {
            val appInfo = context.applicationInfo
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Detects Xposed / LSPosed / EdXposed framework presence.
     */
    private fun isXposedPresent(): Boolean {
        // Check for Xposed-related stack frames
        try {
            val stackTrace = Thread.currentThread().stackTrace
            for (element in stackTrace) {
                if (element.className.contains("de.robv.android.xposed") ||
                    element.className.contains("com.android.internal.os.ZygoteInit")
                ) {
                    // ZygoteInit alone is normal; combined with xposed classes is suspicious
                    if (element.className.contains("xposed")) return true
                }
            }
        } catch (_: Exception) { /* ignore */ }

        // Check for Xposed-related packages
        val xposedPackages = listOf(
            "de.robv.android.xposed.installer",
            "org.lsposed.manager",
            "com.solohsu.android.edxp.manager",
            "org.meowcat.edxposed.manager",
        )
        if (xposedPackages.any { isPackageInstalled(it) }) return true

        // Check for Xposed class in runtime
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    /**
     * Detects known APK patching / piracy tools.
     */
    private fun hasPatchingTools(): Boolean {
        val patchingPackages = listOf(
            "com.chelpus.lackypatch",
            "com.dimonvideo.luckypatcher",
            "com.forpda.lp",
            "com.android.protips",
            "cc.madkite.freedom",
            "com.happymod.apk",
            "org.creeplays.hack",
        )
        return patchingPackages.any { isPackageInstalled(it) }
    }

    /**
     * Verifies the APK signing certificate matches the expected one.
     * A mismatch indicates the APK has been repackaged / resigned.
     */
    @Suppress("DEPRECATION")
    private fun isSignatureMismatch(): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                Timber.w("No signatures found — possible unsigned APK")
                return true
            }

            val expectedHash = securityConfig.releaseCertHash
            if (expectedHash.isBlank()) {
                // No expected hash configured — skip comparison.
                // This is normal during development; in production,
                // set RELEASE_CERT_HASH in keystore.properties.
                Timber.d("Signature check: no expected hash configured, skipping")
                return false
            }

            val actualHash = sha256Hex(signatures[0].toByteArray())
            val mismatch = !actualHash.equals(expectedHash, ignoreCase = true)
            if (mismatch) {
                Timber.w("Signature mismatch! Expected=$expectedHash, Actual=$actualHash")
            }
            mismatch
        } catch (e: Exception) {
            Timber.w(e, "Signature verification failed")
            false // Don't flag as tampered on error
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Computes a colon-separated SHA-256 hex string from raw bytes.
     * E.g. "AB:CD:EF:12:..."
     */
    private fun sha256Hex(bytes: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
