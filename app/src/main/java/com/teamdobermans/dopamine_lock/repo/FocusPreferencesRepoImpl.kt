package com.teamdobermans.dopamine_lock.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teamdobermans.dopamine_lock.model.FocusPreferences
import com.teamdobermans.dopamine_lock.model.PomodoroPresetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "focus_preferences"
)

class FocusPreferencesRepositoryImpl(
    context: Context
) : FocusPreferencesRepository {
    private val dataStore = context.applicationContext.focusPreferencesDataStore

    override fun observePreferences(): Flow<FocusPreferences> {
        return dataStore.data.map { store ->
            FocusPreferences(
                presetType = store[KEY_PRESET_TYPE]?.let { runCatching { PomodoroPresetType.valueOf(it) }.getOrNull() }
                    ?: PomodoroPresetType.STANDARD,
                focusMinutes = store[KEY_FOCUS_MINUTES] ?: 25,
                shortBreakMinutes = store[KEY_SHORT_BREAK_MINUTES] ?: 5,
                longBreakMinutes = store[KEY_LONG_BREAK_MINUTES] ?: 15,
                autoStartBreak = store[KEY_AUTO_START_BREAK] ?: false,
                autoStartNextSession = store[KEY_AUTO_START_NEXT_SESSION] ?: false
            )
        }
    }

    override suspend fun savePreferences(preferences: FocusPreferences): Result<Unit> = runCatching {
        dataStore.edit { store ->
            store[KEY_PRESET_TYPE] = preferences.presetType.name
            store[KEY_FOCUS_MINUTES] = preferences.focusMinutes.coerceIn(1, 180)
            store[KEY_SHORT_BREAK_MINUTES] = preferences.shortBreakMinutes.coerceIn(1, 60)
            store[KEY_LONG_BREAK_MINUTES] = preferences.longBreakMinutes.coerceIn(1, 90)
            store[KEY_AUTO_START_BREAK] = preferences.autoStartBreak
            store[KEY_AUTO_START_NEXT_SESSION] = preferences.autoStartNextSession
        }
    }

    private companion object {
        val KEY_PRESET_TYPE = stringPreferencesKey("preset_type")
        val KEY_FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val KEY_SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
        val KEY_LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val KEY_AUTO_START_BREAK = booleanPreferencesKey("auto_start_break")
        val KEY_AUTO_START_NEXT_SESSION = booleanPreferencesKey("auto_start_next_session")
    }
}
