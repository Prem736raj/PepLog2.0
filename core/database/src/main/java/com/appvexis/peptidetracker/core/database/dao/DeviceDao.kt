package com.appvexis.peptidetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appvexis.peptidetracker.core.database.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Database access object for Device settings and entitlements.
 */
@Dao
interface DeviceDao {
    @Query("SELECT * FROM device_info LIMIT 1")
    fun getDeviceInfo(): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceInfo(device: DeviceEntity)

    @Update
    suspend fun updateDeviceInfo(device: DeviceEntity)

    @Query("UPDATE device_info SET trial_credits_remaining = :credits")
    suspend fun updateTrialCredits(credits: Int)

    @Query("UPDATE device_info SET is_premium = :isPremium, subscription_type = :type, subscription_expiry = :expiry")
    suspend fun updatePremiumStatus(isPremium: Boolean, type: String?, expiry: Long?)

    @Query("UPDATE device_info SET google_user_id = :userId, google_email = :email")
    suspend fun linkGoogleAccount(userId: String, email: String)
}
