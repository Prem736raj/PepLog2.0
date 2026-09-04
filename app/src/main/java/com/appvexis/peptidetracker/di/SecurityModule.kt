package com.appvexis.peptidetracker.di

import com.appvexis.peptidetracker.BuildConfig
import com.appvexis.peptidetracker.core.common.security.SecurityConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides app-level security configuration to the :core:common security
 * layer. Values are sourced from BuildConfig which reads from
 * keystore.properties at build time.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurityConfig(): SecurityConfig = SecurityConfig(
        releaseCertHash = BuildConfig.RELEASE_CERT_HASH,
    )
}
