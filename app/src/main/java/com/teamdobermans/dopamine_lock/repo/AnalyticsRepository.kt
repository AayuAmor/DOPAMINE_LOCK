package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.AnalyticsSummary

interface AnalyticsRepository {
    suspend fun getAnalyticsSummary(): Result<AnalyticsSummary>

    suspend fun getWeeklyAnalytics(): Result<List<Double>>

    suspend fun getMonthlyAnalytics(): Result<List<Double>>

    suspend fun getFocusDistribution(): Result<Map<String, Double>>

    suspend fun getSuccessRate(): Result<Int>

    suspend fun getBestFocusDay(): Result<String>

    suspend fun getFocusTrend(): Result<List<Double>>

    suspend fun getGoalStatistics(): Result<Map<String, Int>>

    suspend fun getDisciplineStatistics(): Result<Map<String, Int>>
}
