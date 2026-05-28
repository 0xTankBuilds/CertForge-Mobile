# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.az104.study.data.local.entity.** { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.az104.study.data.remote.SyncApi

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
