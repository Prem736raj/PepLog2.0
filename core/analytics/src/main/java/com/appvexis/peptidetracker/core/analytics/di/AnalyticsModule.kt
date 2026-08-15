package com.appvexis.peptidetracker.core.analytics.di

import android.content.Context
import com.appvexis.peptidetracker.core.analytics.AnalyticsEngine
import com.appvexis.peptidetracker.core.analytics.worker.AnalyticsWorkerScheduler
import com.appvexis.peptidetracker.core.model.repository.AnalyticsRepository
import com.appvexis.peptidetracker.core.model.repository.ProtocolRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsEngine(
        analyticsRepository: AnalyticsRepository,
        protocolRepository: ProtocolRepository
    ): AnalyticsEngine {
        return AnalyticsEngine(analyticsRepository, protocolRepository)
    }

    @Provides
    @Singleton
    fun provideAnalyticsWorkerScheduler(
        @ApplicationContext context: Context
    ): AnalyticsWorkerScheduler {
        return AnalyticsWorkerScheduler(context)
    }
}
