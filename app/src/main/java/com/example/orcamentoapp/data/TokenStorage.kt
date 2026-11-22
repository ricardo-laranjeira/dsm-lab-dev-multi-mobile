package com.example.orcamentoapp.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "sessao_segura",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun salvarToken(token: String) {
        preferences.edit {
            putString("access_token", token)
        }
    }

    fun obterToken(): String? =
        preferences.getString("access_token", null)

    fun limparToken() {
        preferences.edit {
            remove("access_token")
        }
    }
}
