package com.az104.study.util

import android.content.Context
import android.content.SharedPreferences
import java.net.URI

class ServerUrlManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun getServerUrl(): URI? {
        val url = prefs.getString(KEY_SERVER_URL, null) ?: return null
        return try {
            URI(url)
        } catch (_: Exception) {
            null
        }
    }

    fun saveServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }

    fun getProfileId(): String? = prefs.getString(KEY_PROFILE_ID, null)

    fun saveProfileId(profileId: String) {
        prefs.edit().putString(KEY_PROFILE_ID, profileId).apply()
    }

    fun getProfileName(): String? = prefs.getString(KEY_PROFILE_NAME, null)

    fun saveProfileName(name: String) {
        prefs.edit().putString(KEY_PROFILE_NAME, name).apply()
    }

    fun getLastSyncTimestamp(): Long = prefs.getLong(KEY_LAST_SYNC, 0)

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getLastProgressSyncTimestamp(): Long = prefs.getLong(KEY_LAST_PROGRESS_SYNC, 0)

    fun setLastProgressSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_PROGRESS_SYNC, timestamp).apply()
    }

    fun clearAll() {
        prefs.edit()
            .remove(KEY_SERVER_URL)
            .remove(KEY_PROFILE_ID)
            .remove(KEY_PROFILE_NAME)
            .remove(KEY_LAST_SYNC)
            .remove(KEY_LAST_PROGRESS_SYNC)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "az104_server_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PROFILE_ID = "profile_id"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_LAST_PROGRESS_SYNC = "last_progress_sync_timestamp"
    }
}
