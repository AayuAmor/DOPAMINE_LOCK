package com.teamdobermans.dopamine_lock.data.repository

import com.teamdobermans.dopamine_lock.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUserProfile(user: User): Result<Unit>

    suspend fun getUserProfile(uid: String): Result<User>

    suspend fun getCurrentUserProfile(): Result<User>

    suspend fun updateUserProfile(user: User): Result<Unit>

    suspend fun updateUserName(uid: String, name: String): Result<Unit>

    suspend fun updateDisciplineScore(uid: String, score: Int): Result<Unit>

    suspend fun updateStreaks(
        uid: String,
        currentStreak: Int,
        bestStreak: Int
    ): Result<Unit>

    suspend fun updateTotalFocusHours(
        uid: String,
        totalFocusHours: Double
    ): Result<Unit>

    fun observeUserProfile(uid: String): Flow<Result<User>>

    suspend fun ensureUserProfileExists(): Result<User>
}
