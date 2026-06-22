package com.teamdobermans.dopamine_lock.repo

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teamdobermans.dopamine_lock.model.EnforcementSettings
import com.teamdobermans.dopamine_lock.model.MissionEnforcementState
import com.teamdobermans.dopamine_lock.service.MissionEnforcementService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.enforcementDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mission_enforcement_preferences"
)

class EnforcementRepositoryImpl(
    private val context: Context
) : EnforcementRepository {
    private val appContext = context.applicationContext
    private val dataStore = appContext.enforcementDataStore

    override suspend fun startEnforcement(
        missionId: String,
        missionTitle: String,
        blockedApps: List<String>,
        startedAt: Long,
        durationMinutes: Int
    ): Result<Unit> = runCatching {
        val normalizedBlockedApps = blockedApps.distinct().filter { it.isNotBlank() }
        dataStore.edit { preferences ->
            preferences[KEY_MISSION_ID] = missionId
            preferences[KEY_MISSION_TITLE] = missionTitle
            preferences[KEY_ACTIVE] = true
            preferences[KEY_BLOCKED_APPS] = normalizedBlockedApps.joinToString(PACKAGE_SEPARATOR)
            preferences[KEY_STARTED_AT] = startedAt
            preferences[KEY_DURATION_MINUTES] = durationMinutes
            preferences[KEY_LAST_BLOCKED_PACKAGE] = ""
            preferences[KEY_BLOCKED_ATTEMPTS] = 0
        }

        if (observeSettings().first().foregroundServiceEnabled) {
            val intent = MissionEnforcementService.startIntent(appContext)
            ContextCompat.startForegroundService(appContext, intent)
        }
    }

    override suspend fun stopEnforcement(): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY_ACTIVE] = false
            preferences[KEY_MISSION_ID] = ""
            preferences[KEY_MISSION_TITLE] = ""
            preferences[KEY_BLOCKED_APPS] = ""
            preferences[KEY_STARTED_AT] = 0L
            preferences[KEY_DURATION_MINUTES] = 0
            preferences[KEY_LAST_BLOCKED_PACKAGE] = ""
            preferences[KEY_BLOCKED_ATTEMPTS] = 0
        }
        appContext.stopService(Intent(appContext, MissionEnforcementService::class.java))
    }

    override suspend fun isMissionActive(): Boolean {
        return observeEnforcementState().first().active && observeSettings().first().blockingEnabled
    }

    override suspend fun getBlockedApps(): List<String> {
        return observeEnforcementState().first().blockedApps
    }

    override suspend fun updateBlockedApps(blockedApps: List<String>): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY_BLOCKED_APPS] = blockedApps.distinct().filter { it.isNotBlank() }
                .joinToString(PACKAGE_SEPARATOR)
        }
    }

    override fun observeEnforcementState(): Flow<MissionEnforcementState> {
        return dataStore.data.map { preferences ->
            MissionEnforcementState(
                missionId = preferences[KEY_MISSION_ID].orEmpty(),
                missionTitle = preferences[KEY_MISSION_TITLE].orEmpty(),
                active = preferences[KEY_ACTIVE] ?: false,
                blockedApps = preferences[KEY_BLOCKED_APPS].orEmpty()
                    .split(PACKAGE_SEPARATOR)
                    .filter { it.isNotBlank() },
                startedAt = preferences[KEY_STARTED_AT] ?: 0L,
                durationMinutes = preferences[KEY_DURATION_MINUTES] ?: 0,
                lastBlockedPackage = preferences[KEY_LAST_BLOCKED_PACKAGE].orEmpty(),
                blockedAttempts = preferences[KEY_BLOCKED_ATTEMPTS] ?: 0
            )
        }
    }

    override fun observeSettings(): Flow<EnforcementSettings> {
        return dataStore.data.map { preferences ->
            EnforcementSettings(
                strictModeEnabled = preferences[KEY_STRICT_MODE] ?: false,
                blockingEnabled = preferences[KEY_BLOCKING_ENABLED] ?: true,
                overlayProtectionEnabled = preferences[KEY_OVERLAY_PROTECTION] ?: true,
                foregroundServiceEnabled = preferences[KEY_FOREGROUND_SERVICE] ?: true
            )
        }
    }

    override suspend fun saveSettings(settings: EnforcementSettings): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY_STRICT_MODE] = settings.strictModeEnabled
            preferences[KEY_BLOCKING_ENABLED] = settings.blockingEnabled
            preferences[KEY_OVERLAY_PROTECTION] = settings.overlayProtectionEnabled
            preferences[KEY_FOREGROUND_SERVICE] = settings.foregroundServiceEnabled
        }
    }

    override suspend fun recordBlockedAttempt(packageName: String): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_BLOCKED_PACKAGE] = packageName
            preferences[KEY_BLOCKED_ATTEMPTS] = (preferences[KEY_BLOCKED_ATTEMPTS] ?: 0) + 1
        }
    }

    private companion object {
        const val PACKAGE_SEPARATOR = "|"
        val KEY_MISSION_ID = stringPreferencesKey("mission_id")
        val KEY_MISSION_TITLE = stringPreferencesKey("mission_title")
        val KEY_ACTIVE = booleanPreferencesKey("active")
        val KEY_BLOCKED_APPS = stringPreferencesKey("blocked_apps")
        val KEY_STARTED_AT = longPreferencesKey("started_at")
        val KEY_DURATION_MINUTES = intPreferencesKey("duration_minutes")
        val KEY_LAST_BLOCKED_PACKAGE = stringPreferencesKey("last_blocked_package")
        val KEY_BLOCKED_ATTEMPTS = intPreferencesKey("blocked_attempts")
        val KEY_STRICT_MODE = booleanPreferencesKey("strict_mode_enabled")
        val KEY_BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val KEY_OVERLAY_PROTECTION = booleanPreferencesKey("overlay_protection_enabled")
        val KEY_FOREGROUND_SERVICE = booleanPreferencesKey("foreground_service_enabled")
    }
}
