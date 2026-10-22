package io.softwarebyandroid.tool.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException

private val Context.settings by preferencesDataStore("settings")
enum class ThemeMode { SYSTEM, LIGHT, DARK }
class Preferences(context: Context) {
    private val store = context.applicationContext.settings
    private val themeKey = stringPreferencesKey("theme")
    val theme = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map {
        runCatching { ThemeMode.valueOf(it[themeKey] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM)
    }
    suspend fun setTheme(theme: ThemeMode) { store.edit { it[themeKey] = theme.name } }
}
