package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.StreakRecord
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    suspend fun evaluateToday(): Result<StreakRecord>

    suspend fun calculateCurrentStreak(): Result<Int>

    suspend fun calculateBestStreak(): Result<Int>

    suspend fun getTodayRecord(): Result<StreakRecord?>

    suspend fun getStreakCalendar(): Result<List<StreakRecord>>

    fun observeCurrentStreak(): Flow<Result<Int>>

    fun observeStreakRecords(): Flow<Result<List<StreakRecord>>>
}
