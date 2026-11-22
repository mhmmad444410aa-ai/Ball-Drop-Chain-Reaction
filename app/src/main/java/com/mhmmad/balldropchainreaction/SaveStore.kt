package com.mhmmad.balldropchainreaction

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.saveDataStore by preferencesDataStore("ball_drop_v1")
data class SaveState(val language: String = "en", val sound: Boolean = true,
                     val unlocked: Int = 1, val stars: Map<Int, Int> = emptyMap())

class SaveStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")
    private val soundKey = booleanPreferencesKey("sfx")
    private val unlockedKey = intPreferencesKey("unlocked")
    private val starPrefix = "stars_"
    val flow = context.saveDataStore.data.map { prefs ->
        val completed = (1..20).mapNotNull { id ->
            val value = prefs[intPreferencesKey(starPrefix + id)] ?: 0
            if (value > 0) id to value else null
        }.toMap()
        SaveState(prefs[languageKey] ?: "en", prefs[soundKey] ?: true,
            prefs[unlockedKey] ?: 1, completed)
    }
    suspend fun setLanguage(lang: String) = context.saveDataStore.edit {
        it[languageKey] = if (lang in listOf("en","ar","pt")) lang else "en"
    }
    suspend fun setSound(enabled: Boolean) = context.saveDataStore.edit { it[soundKey] = enabled }
    suspend fun recordWin(level: Int, stars: Int) = context.saveDataStore.edit {
        val key = intPreferencesKey(starPrefix + level)
        it[key] = maxOf(stars, it[key] ?: 0)
        it[unlockedKey] = maxOf(level + 1, it[unlockedKey] ?: 1).coerceAtMost(20)
    }
}
