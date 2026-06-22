package com.teamdobermans.dopamine_lock.model

data class AnalyticsSummary(
    val totalFocusHours: Double = 0.0,
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val failedSessions: Int = 0,
    val successRate: Int = 0,
    val bestFocusDay: String = "",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val disciplineScore: Int = 0,
    val completedGoals: Int = 0,
    val focusDistribution: Map<String, Double> = emptyMap(),
    val weeklyFocusHours: List<Double> = emptyList(),
    val monthlyFocusHours: List<Double> = emptyList()
)
