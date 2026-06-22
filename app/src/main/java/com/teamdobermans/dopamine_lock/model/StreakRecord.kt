package com.teamdobermans.dopamine_lock.model

data class StreakRecord(
    val recordId: String = "",
    val userId: String = "",
    val date: String = "",
    val successful: Boolean = false,
    val missionsCompleted: Int = 0,
    val focusMinutes: Int = 0,
    val dailyGoalMinutes: Int = 0,
    val createdAt: Long = 0L
)
