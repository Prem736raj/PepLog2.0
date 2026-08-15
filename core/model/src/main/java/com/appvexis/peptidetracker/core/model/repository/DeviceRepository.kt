package com.appvexis.peptidetracker.core.model.repository

import com.appvexis.peptidetracker.core.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining device status, entitlements, and credit operations.
 */
interface DeviceRepository {
    fun getDeviceInfo(): Flow<DeviceInfo?>
    suspend fun registerDevice(deviceId: String, timestamp: Long)
    suspend fun updateTrialCredits(remainingCredits: Int)
    suspend fun setPremiumStatus(isPremium: Boolean, type: String?, expiry: Long?)
    suspend fun linkGoogleAccount(userId: String, email: String)
}
