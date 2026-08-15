package com.appvexis.peptidetracker.core.database.repository

import com.appvexis.peptidetracker.core.database.dao.DeviceDao
import com.appvexis.peptidetracker.core.database.entity.DeviceEntity
import com.appvexis.peptidetracker.core.database.entity.toDomain
import com.appvexis.peptidetracker.core.model.DeviceInfo
import com.appvexis.peptidetracker.core.model.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database implementation of [DeviceRepository] mapping database operations to Domain.
 */
@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao
) : DeviceRepository {

    override fun getDeviceInfo(): Flow<DeviceInfo?> {
        return deviceDao.getDeviceInfo().map { it?.toDomain() }
    }

    override suspend fun registerDevice(deviceId: String, timestamp: Long) {
        deviceDao.insertDeviceInfo(
            DeviceEntity(
                deviceId = deviceId,
                firstInstallTimestamp = timestamp,
                trialCreditsRemaining = 200,
                isPremium = false,
                subscriptionType = null,
                subscriptionExpiry = null,
                googleUserId = null,
                googleEmail = null
            )
        )
    }

    override suspend fun updateTrialCredits(remainingCredits: Int) {
        deviceDao.updateTrialCredits(remainingCredits)
    }

    override suspend fun setPremiumStatus(isPremium: Boolean, type: String?, expiry: Long?) {
        deviceDao.updatePremiumStatus(isPremium, type, expiry)
    }

    override suspend fun linkGoogleAccount(userId: String, email: String) {
        deviceDao.linkGoogleAccount(userId, email)
    }
}
