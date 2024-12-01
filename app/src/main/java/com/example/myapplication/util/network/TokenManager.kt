package com.example.myapplication.util.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(private val context: Context) {
    private val encryptedSharedPrefs by lazy {
        createEncryptedSharedPrefs()
    }

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    private fun createEncryptedSharedPrefs(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        encryptedSharedPrefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPrefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken() {
        encryptedSharedPrefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }
}

