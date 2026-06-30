package com.teamdobermans.dopamine_lock.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.teamdobermans.dopamine_lock.model.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

class AppPreferencesRepositoryImpl(
    context: Context
) : AppPreferencesRepository {
    private val dataStore = context.applicationContext.appPreferencesDataStore

    override fun observePreferences(): Flow<AppPreferences> {
        return dataStore.data.map { store ->
            AppPreferences(
                hasCompletedOnboarding = store[KEY_ONBOARDING_COMPLETED] ?: false
            )
        }
    }

    override suspend fun setOnboardingCompleted(): Result<Unit> = runCatching {
        dataStore.edit { store ->
            store[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    private companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
