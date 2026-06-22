package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.GoalType
import com.teamdobermans.dopamine_lock.model.GoalUnit
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun createGoal(
        title: String,
        description: String,
        goalType: GoalType,
        targetValue: Int,
        unit: GoalUnit
    ): Result<Goal>

    suspend fun updateGoal(goal: Goal): Result<Goal>

    suspend fun deleteGoal(goalId: String): Result<Unit>

    suspend fun completeGoal(goalId: String): Result<Goal>

    suspend fun getGoal(goalId: String): Result<Goal>

    suspend fun getCurrentUserGoals(): Result<List<Goal>>

    suspend fun getDailyGoals(): Result<List<Goal>>

    suspend fun getWeeklyGoals(): Result<List<Goal>>

    suspend fun getMonthlyGoals(): Result<List<Goal>>

    suspend fun updateProgress(unit: GoalUnit, amount: Int): Result<List<Goal>>

    fun observeGoals(): Flow<Result<List<Goal>>>

    fun observeActiveGoals(): Flow<Result<List<Goal>>>
}
