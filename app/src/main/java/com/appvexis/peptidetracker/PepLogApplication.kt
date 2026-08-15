package com.appvexis.peptidetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.appvexis.peptidetracker.core.analytics.worker.AnalyticsWorkerScheduler
import com.appvexis.peptidetracker.core.common.security.AppIntegrityChecker
import com.appvexis.peptidetracker.core.common.security.TamperDetectionManager
import com.appvexis.peptidetracker.feature.health.worker.HealthSyncScheduler
import com.appvexis.peptidetracker.logging.CrashReportingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class PepLogApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var analyticsWorkerScheduler: AnalyticsWorkerScheduler

    @Inject
    lateinit var healthSyncScheduler: HealthSyncScheduler

    @Inject
    lateinit var appIntegrityChecker: AppIntegrityChecker

    @Inject
    lateinit var tamperDetectionManager: TamperDetectionManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        // Security checks
        appIntegrityChecker.checkIntegrity()
        tamperDetectionManager.runChecks()

        // Schedule nightly precomputation of analytics summaries
        analyticsWorkerScheduler.scheduleNightlyRecompute()
        // Schedule periodic Health Connect sync (every 6 hours)
        healthSyncScheduler.schedulePeriodicSync()
    }
}
