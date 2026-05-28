package com.az104.study.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        TOKEN_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiToken(): String? = prefs.getString(KEY_API_TOKEN, null)

    fun saveApiToken(token: String) {
        prefs.edit().putString(KEY_API_TOKEN, token).apply()
    }

    fun clearApiToken() {
        prefs.edit().remove(KEY_API_TOKEN).apply()
    }

    fun isPaired(): Boolean = getApiToken() != null

    companion object {
        private const val TOKEN_PREFS_NAME = "az104_secure_prefs"
        private const val KEY_API_TOKEN = "api_token"
    }
}
