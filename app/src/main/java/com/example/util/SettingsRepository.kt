package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Ekstensi untuk membuat DataStore singleton
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "super_tools_settings")

// 2. Sealed class untuk mendefinisikan Key dan Default Value secara type-safe
sealed class SettingKey<T>(val key: Preferences.Key<T>, val defaultValue: T) {
    class BooleanKey(name: String, defaultValue: Boolean) : SettingKey<Boolean>(booleanPreferencesKey(name), defaultValue)
    class StringKey(name: String, defaultValue: String) : SettingKey<String>(stringPreferencesKey(name), defaultValue)
    class IntKey(name: String, defaultValue: Int) : SettingKey<Int>(intPreferencesKey(name), defaultValue)
}

// 3. Kumpulan Setting Keys yang terorganisir per kategori
object SettingsKeys {
    // Umum
    val THEME_MODE = SettingKey.StringKey("theme_mode", "system") // system, light, dark
    val APP_LANGUAGE = SettingKey.StringKey("app_language", "id") // id, en
    val NOTIFICATIONS_ENABLED = SettingKey.BooleanKey("notifications_enabled", true)

    // Keamanan & Privasi
    val APP_LOCK_ENABLED = SettingKey.BooleanKey("app_lock_enabled", false)
    val REQUIRE_BIOMETRIC = SettingKey.BooleanKey("require_biometric", false)

    // Penyimpanan
    val DOWNLOAD_LOCATION = SettingKey.StringKey("download_location", "downloads")
    val AUTO_CLEANUP_CACHE = SettingKey.BooleanKey("auto_cleanup_cache", false)
    val MAX_CACHE_SIZE_MB = SettingKey.IntKey("max_cache_size_mb", 500)

    // AI & Editor
    val DEFAULT_AI_PROVIDER = SettingKey.StringKey("default_ai_provider", "gemini")
    val AI_USE_CELLULAR = SettingKey.BooleanKey("ai_use_cellular", true)

    // Auto Upload
    val AUTO_UPLOAD_WIFI_ONLY = SettingKey.BooleanKey("auto_upload_wifi_only", true)
    
    // Akun
    val REMEMBER_ME = SettingKey.BooleanKey("remember_me", true)
}

// 4. Interface Repository
interface SettingsRepository {
    fun <T> getSetting(key: SettingKey<T>): Flow<T>
    suspend fun <T> setSetting(key: SettingKey<T>, value: T)
    suspend fun resetCategory(keys: List<SettingKey<*>>)
    suspend fun resetAll()
}

// 5. Implementasi Repository
class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    
    override fun <T> getSetting(key: SettingKey<T>): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key.key] ?: key.defaultValue
        }
    }

    override suspend fun <T> setSetting(key: SettingKey<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key.key] = value
        }
    }

    override suspend fun resetCategory(keys: List<SettingKey<*>>) {
        dataStore.edit { preferences ->
            keys.forEach { settingKey ->
                @Suppress("UNCHECKED_CAST")
                preferences.remove(settingKey.key as Preferences.Key<Any>)
            }
        }
    }

    override suspend fun resetAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
