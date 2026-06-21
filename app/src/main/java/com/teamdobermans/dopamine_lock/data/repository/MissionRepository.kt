package com.teamdobermans.dopamine_lock.data.repository

import com.teamdobermans.dopamine_lock.domain.model.Mission
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    suspend fun createMission(
        title: String,
        goal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>
    ): Result<Mission>

    suspend fun startMission(missionId: String): Result<Mission>

    suspend fun completeMission(missionId: String): Result<Mission>

    suspend fun abandonMission(missionId: String): Result<Mission>

    suspend fun failMission(missionId: String): Result<Mission>

    suspend fun getMission(missionId: String): Result<Mission>

    suspend fun getCurrentUserMissions(): Result<List<Mission>>

    fun observeActiveMission(): Flow<Result<Mission?>>

    fun observeUserMissions(): Flow<Result<List<Mission>>>
}
