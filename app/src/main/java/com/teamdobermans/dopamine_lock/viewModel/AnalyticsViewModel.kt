package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.repo.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getAnalyticsSummary()
                .onSuccess { summary ->
                    val goalStats = repository.getGoalStatistics().getOrDefault(emptyMap())
                    val disciplineStats = repository.getDisciplineStatistics().getOrDefault(emptyMap())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            weeklyHours = summary.weeklyFocusHours,
                            monthlyHours = summary.monthlyFocusHours,
                            focusDistribution = summary.focusDistribution,
                            bestFocusDay = summary.bestFocusDay,
                            successRate = summary.successRate,
                            goalCompletionRate = goalStats["completionRate"] ?: 0,
                            disciplineGrowth = disciplineStats["growth"] ?: 0
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to load analytics.") }
        }
    }

    fun refreshAnalytics() {
        loadAnalytics()
    }

    fun loadWeeklyAnalytics() {
        viewModelScope.launch {
            repository.getWeeklyAnalytics()
                .onSuccess { weeklyHours -> _uiState.update { it.copy(weeklyHours = weeklyHours) } }
                .onFailure { exception -> showFailure(exception, "Failed to load weekly analytics.") }
        }
    }

    fun loadMonthlyAnalytics() {
        viewModelScope.launch {
            repository.getMonthlyAnalytics()
                .onSuccess { monthlyHours -> _uiState.update { it.copy(monthlyHours = monthlyHours) } }
                .onFailure { exception -> showFailure(exception, "Failed to load monthly analytics.") }
        }
    }

    fun loadDistribution() {
        viewModelScope.launch {
            repository.getFocusDistribution()
                .onSuccess { distribution -> _uiState.update { it.copy(focusDistribution = distribution) } }
                .onFailure { exception -> showFailure(exception, "Failed to load focus distribution.") }
        }
    }

    fun clear() {
        _uiState.value = AnalyticsUiState()
    }

    private fun showFailure(exception: Throwable, fallback: String) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = exception.message ?: fallback)
        }
    }
}

class AnalyticsViewModelFactory(
    private val repository: AnalyticsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
