# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Moshi models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Coroutines
-keepclassmembers class * {
    @kotlinx.coroutines.* <methods>;
}

# Keep Data Models & Serialized classes
-keepclassmembers class com.example.data.** { *; }
-keepclassmembers class com.example.core.** { *; }

# Suppress warnings
-dontwarn okhttp3.**
-dontwarn retrofit2.**

