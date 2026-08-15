package com.appvexis.peptidetracker.logging

import android.util.Log
import timber.log.Timber

/**
 * Release-mode Timber tree that suppresses DEBUG/VERBOSE logs and
 * records warnings and errors for crash reporting.
 *
 * Integration point for crash reporting services:
 * - **Firebase Crashlytics**: Uncomment the Crashlytics calls below
 *   after adding the `com.google.firebase:firebase-crashlytics` dependency.
 * - **Sentry**: Replace with Sentry.captureException() calls.
 *
 * This tree is planted in [PepLogApplication] for non-debug builds,
 * ensuring no debug logs leak to production while still capturing
 * actionable error information.
 */
class CrashReportingTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log WARN, ERROR, and ASSERT in release
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Log to the crash reporting service
        // Uncomment these lines after adding Firebase Crashlytics:
        //
        // Firebase.crashlytics.log("${priorityLabel(priority)}/$tag: $message")
        // if (t != null) {
        //     Firebase.crashlytics.recordException(t)
        // }

        // For now, use Android system log as a fallback
        if (t != null) {
            when (priority) {
                Log.ERROR -> Log.e(tag, message, t)
                Log.WARN -> Log.w(tag, message, t)
                else -> Log.w(tag, message, t)
            }
        }
    }

    @Suppress("unused")
    private fun priorityLabel(priority: Int): String = when (priority) {
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> "?"
    }
}
