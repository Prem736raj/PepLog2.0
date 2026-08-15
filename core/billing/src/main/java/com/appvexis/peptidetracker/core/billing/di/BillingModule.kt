package com.appvexis.peptidetracker.core.billing.di

import android.content.Context
import com.appvexis.peptidetracker.core.billing.BillingClientWrapper
import com.appvexis.peptidetracker.core.billing.SubscriptionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingClientWrapper(
        @ApplicationContext context: Context
    ): BillingClientWrapper {
        return BillingClientWrapper(context)
    }

    @Provides
    @Singleton
    fun provideSubscriptionManager(
        @ApplicationContext context: Context,
        billingClientWrapper: BillingClientWrapper
    ): SubscriptionManager {
        return SubscriptionManager(context, billingClientWrapper)
    }
}
