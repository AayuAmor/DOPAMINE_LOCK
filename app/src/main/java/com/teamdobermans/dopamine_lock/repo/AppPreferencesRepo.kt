package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.AppPreferences
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observePreferences(): Flow<AppPreferences>
    suspend fun setOnboardingCompleted(): Result<Unit>
}
