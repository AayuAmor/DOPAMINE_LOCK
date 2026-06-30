package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.FocusPreferences
import kotlinx.coroutines.flow.Flow

interface FocusPreferencesRepository {
    fun observePreferences(): Flow<FocusPreferences>
    suspend fun savePreferences(preferences: FocusPreferences): Result<Unit>
}
