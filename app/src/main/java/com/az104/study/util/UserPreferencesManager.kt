package com.az104.study.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "az104_settings")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val homeWifiSsid: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[HOME_WIFI_SSID]
    }

    val darkMode: Flow<DarkModePreference> = context.dataStore.data.map { prefs ->
        when (prefs[DARK_MODE]) {
            "light" -> DarkModePreference.LIGHT
            "dark" -> DarkModePreference.DARK
            else -> DarkModePreference.SYSTEM
        }
    }

    suspend fun setHomeWifiSsid(ssid: String) {
        context.dataStore.edit { prefs ->
            prefs[HOME_WIFI_SSID] = ssid
        }
    }

    suspend fun setDarkMode(mode: DarkModePreference) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = mode.value
        }
    }

    companion object {
        private val HOME_WIFI_SSID = stringPreferencesKey("home_wifi_ssid")
        private val DARK_MODE = stringPreferencesKey("dark_mode")
    }
}

enum class DarkModePreference(val value: String) {
    SYSTEM("system"), LIGHT("light"), DARK("dark")
}
