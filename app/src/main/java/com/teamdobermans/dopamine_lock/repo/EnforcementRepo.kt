package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.EnforcementSettings
import com.teamdobermans.dopamine_lock.model.MissionEnforcementState
import kotlinx.coroutines.flow.Flow

interface EnforcementRepository {
    suspend fun startEnforcement(
        missionId: String,
        missionTitle: String,
        blockedApps: List<String>,
        startedAt: Long,
        durationMinutes: Int
    ): Result<Unit>

    suspend fun stopEnforcement(): Result<Unit>

    suspend fun isMissionActive(): Boolean

    suspend fun getBlockedApps(): List<String>

    suspend fun updateBlockedApps(blockedApps: List<String>): Result<Unit>

    fun observeEnforcementState(): Flow<MissionEnforcementState>

    fun observeSettings(): Flow<EnforcementSettings>

    suspend fun saveSettings(settings: EnforcementSettings): Result<Unit>

    suspend fun recordBlockedAttempt(packageName: String): Result<Unit>
}
