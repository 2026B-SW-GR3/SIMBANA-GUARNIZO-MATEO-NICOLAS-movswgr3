package com.example.taller4.secrets

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "secrets_datastore")

/**
 * Módulo académico de almacenamiento seguro/configuración.
 *
 * SharedPreferences: simple y síncrono, útil para configuraciones no sensibles.
 * DataStore: moderno y asíncrono, ideal para preferencias con coroutines.
 * EncryptedSharedPreferences: cifra llaves y valores automáticamente,
 * pensado para tokens o API keys.
 *
 * No implementamos Android Keystore directamente porque EncryptedSharedPreferences
 * ya lo usa internamente para gestionar la clave maestra sin añadir complejidad extra.
 */
class SecretStore(private val context: Context) {
    enum class Backend(val label: String) {
        SharedPreferences("SharedPreferences"),
        DataStore("DataStore"),
        EncryptedSharedPreferences("EncryptedSharedPreferences"),
    }

    fun saveSync(key: String, value: String, backend: Backend): Boolean {
        return when (backend) {
            Backend.SharedPreferences -> {
                // SharedPreferences: guarda Strings de forma simple y síncrona.
                context.getSharedPreferences("secrets_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString(key, value)
                    .commit()
            }
            Backend.EncryptedSharedPreferences -> {
                // EncryptedSharedPreferences: guarda datos cifrados automáticamente.
                // Útil para información sensible como tokens o API keys.
                val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val prefs = EncryptedSharedPreferences.create(
                    "secrets_encrypted",
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
                prefs.edit().putString(key, value).commit()
            }
            Backend.DataStore -> false
        }
    }

    suspend fun saveDataStore(key: String, value: String): Boolean {
        // DataStore: guardado asíncrono con coroutines.
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
        return true
    }

    suspend fun getValue(key: String, backend: Backend): String? {
        return when (backend) {
            Backend.SharedPreferences -> {
                // Lectura simple para configuraciones no sensibles.
                context.getSharedPreferences("secrets_prefs", Context.MODE_PRIVATE)
                    .getString(key, null)
            }
            Backend.EncryptedSharedPreferences -> {
                // Lectura de datos cifrados automáticamente.
                val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val prefs = EncryptedSharedPreferences.create(
                    "secrets_encrypted",
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
                prefs.getString(key, null)
            }
            Backend.DataStore -> {
                // DataStore: lectura asíncrona del valor asociado a la llave.
                val prefKey = stringPreferencesKey(key)
                val prefs = context.dataStore.data.first()
                prefs[prefKey]
            }
        }
    }
}

