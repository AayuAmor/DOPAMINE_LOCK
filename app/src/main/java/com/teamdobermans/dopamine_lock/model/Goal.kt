package com.teamdobermans.dopamine_lock.model

data class Goal(
    val goalId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val goalType: GoalType = GoalType.DAILY,
    val targetValue: Int = 0,
    val currentProgress: Int = 0,
    val unit: GoalUnit = GoalUnit.HOURS,
    val completed: Boolean = false,
    val createdAt: Long = 0L,
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val completedAt: Long = 0L
)
