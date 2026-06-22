package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.Goal

data class GoalUiState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val dailyGoals: List<Goal> = emptyList(),
    val weeklyGoals: List<Goal> = emptyList(),
    val monthlyGoals: List<Goal> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)
