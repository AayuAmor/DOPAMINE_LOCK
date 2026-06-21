package com.teamdobermans.dopamine_lock.data.repository

import com.teamdobermans.dopamine_lock.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    suspend fun startSession(
        missionName: String,
        missionGoal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>
    ): Result<FocusSession>

    suspend fun completeSession(
        sessionId: String,
        elapsedSeconds: Long
    ): Result<FocusSession>

    suspend fun abandonSession(
        sessionId: String,
        elapsedSeconds: Long
    ): Result<FocusSession>

    suspend fun getSessionById(sessionId: String): Result<FocusSession>

    suspend fun getCurrentUserSessions(): Result<List<FocusSession>>

    suspend fun getSessionsByDateRange(
        startTime: Long,
        endTime: Long
    ): Result<List<FocusSession>>

    fun observeCurrentUserSessions(): Flow<Result<List<FocusSession>>>

    fun observeActiveSession(): Flow<Result<FocusSession?>>

    suspend fun deleteSession(sessionId: String): Result<Unit>
}
