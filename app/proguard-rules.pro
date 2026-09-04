# ============================================================
# PepLog ProGuard/R8 Rules
# ============================================================
# Comprehensive rules for all libraries used in the project.
# R8 is the default shrinker for Android Gradle Plugin 3.4+.

# ============================================================
# General Android
# ============================================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures (needed by Kotlin, Room, Hilt)
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# ============================================================
# Kotlin
# ============================================================

-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**inlined**

# Kotlin Serialization
-keepattributes RuntimeVisibleAnnotations
-keep,includedescriptorclasses class com.appvexis.peptidetracker.**$$serializer { *; }
-keepclassmembers class com.appvexis.peptidetracker.** {
    *** Companion;
}
-keepclasseswithmembers class com.appvexis.peptidetracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ============================================================
# Jetpack Compose
# ============================================================

# Compose generates lambda classes that must be kept
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# ============================================================
# Room Database
# ============================================================

-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ============================================================
# Hilt / Dagger
# ============================================================

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ============================================================
# Google Play Billing
# ============================================================

-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ============================================================
# Health Connect
# ============================================================

-keep class androidx.health.connect.** { *; }
-dontwarn androidx.health.connect.**
-keep class androidx.health.platform.** { *; }
-dontwarn androidx.health.platform.**

# ============================================================
# Coil (Image Loading)
# ============================================================

-dontwarn coil.**
-keep class coil.** { *; }

# ============================================================
# Vico Charts
# ============================================================

-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ============================================================
# Timber
# ============================================================

# Strip debug/verbose logging in release
-assumenosideeffects class timber.log.Timber {
    public static void d(...);
    public static void v(...);
}

# ============================================================
# DataStore
# ============================================================

-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ============================================================
# WorkManager
# ============================================================

-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ============================================================
# PepLog App — Keep entities and backup models for serialization
# ============================================================

# Keep all Room entities (they use reflection for column mapping)
-keep class com.appvexis.peptidetracker.core.database.entity.** { *; }

# Keep backup models (serialized to JSON)
-keep class com.appvexis.peptidetracker.core.backup.model.** { *; }

# Keep security classes (accessed via Hilt injection)
-keep class com.appvexis.peptidetracker.core.common.security.** { *; }

# ============================================================
# Miscellaneous
# ============================================================

# OkHttp (used by selected Android libraries)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Javax annotations
-dontwarn javax.annotation.**

# Legacy optional HTTP/XML warnings from Android libraries.
-dontwarn org.apache.http.**
-dontwarn android.net.http.**
-dontwarn javax.xml.**
-dontwarn org.codehaus.**
