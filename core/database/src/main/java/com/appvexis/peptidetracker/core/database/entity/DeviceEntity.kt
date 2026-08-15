package com.appvexis.peptidetracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appvexis.peptidetracker.core.model.DeviceInfo

/**
 * Room Database Entity representing the device information and entitlements.
 */
@Entity(tableName = "device_info")
data class DeviceEntity(
    @PrimaryKey 
    @ColumnInfo(name = "device_id") 
    val deviceId: String,
    
    @ColumnInfo(name = "first_install_timestamp") 
    val firstInstallTimestamp: Long,
    
    @ColumnInfo(name = "trial_credits_remaining") 
    val trialCreditsRemaining: Int,
    
    @ColumnInfo(name = "is_premium") 
    val isPremium: Boolean,
    
    @ColumnInfo(name = "subscription_type") 
    val subscriptionType: String?,
    
    @ColumnInfo(name = "subscription_expiry") 
    val subscriptionExpiry: Long?,
    
    @ColumnInfo(name = "google_user_id") 
    val googleUserId: String?,
    
    @ColumnInfo(name = "google_email") 
    val googleEmail: String?
)

fun DeviceEntity.toDomain() = DeviceInfo(
    deviceId = deviceId,
    firstInstallTimestamp = firstInstallTimestamp,
    trialCreditsRemaining = trialCreditsRemaining,
    isPremium = isPremium,
    subscriptionType = subscriptionType,
    subscriptionExpiry = subscriptionExpiry,
    googleUserId = googleUserId,
    googleEmail = googleEmail
)

fun DeviceInfo.toEntity() = DeviceEntity(
    deviceId = deviceId,
    firstInstallTimestamp = firstInstallTimestamp,
    trialCreditsRemaining = trialCreditsRemaining,
    isPremium = isPremium,
    subscriptionType = subscriptionType,
    subscriptionExpiry = subscriptionExpiry,
    googleUserId = googleUserId,
    googleEmail = googleEmail
)
