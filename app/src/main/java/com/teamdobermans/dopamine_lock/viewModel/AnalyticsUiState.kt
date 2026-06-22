package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.AnalyticsSummary

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val summary: AnalyticsSummary = AnalyticsSummary(),
    val weeklyHours: List<Double> = emptyList(),
    val monthlyHours: List<Double> = emptyList(),
    val focusDistribution: Map<String, Double> = emptyMap(),
    val bestFocusDay: String = "",
    val successRate: Int = 0,
    val goalCompletionRate: Int = 0,
    val disciplineGrowth: Int = 0,
    val errorMessage: String? = null
)
