# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.certforge.app.data.local.entity.** { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.certforge.app.data.remote.SyncApi

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
