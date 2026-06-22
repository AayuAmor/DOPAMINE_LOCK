package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.GoalType
import com.teamdobermans.dopamine_lock.model.GoalUnit
import com.teamdobermans.dopamine_lock.repo.GoalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalViewModel(
    private val repository: GoalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private var goalsJob: Job? = null

    fun createGoal(
        title: String,
        description: String,
        goalType: GoalType,
        targetValue: Int,
        unit: GoalUnit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.createGoal(title, description, goalType, targetValue, unit)
                .onSuccess { goal ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Goal created.",
                            goals = (state.goals + goal).distinctBy { it.goalId }
                        ).withDerivedGoals()
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to create goal.") }
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.updateGoal(goal)
                .onSuccess { updatedGoal ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Goal updated.",
                            goals = state.goals.map { if (it.goalId == updatedGoal.goalId) updatedGoal else it }
                        ).withDerivedGoals()
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to update goal.") }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.deleteGoal(goalId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Goal deleted.",
                            goals = state.goals.filterNot { it.goalId == goalId }
                        ).withDerivedGoals()
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to delete goal.") }
        }
    }

    fun completeGoal(goalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.completeGoal(goalId)
                .onSuccess { completedGoal ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = "Goal completed.",
                            goals = state.goals.map { if (it.goalId == completedGoal.goalId) completedGoal else it }
                        ).withDerivedGoals()
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to complete goal.") }
        }
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCurrentUserGoals()
                .onSuccess { goals ->
                    _uiState.update { it.copy(isLoading = false, goals = goals).withDerivedGoals() }
                }
                .onFailure { exception -> showFailure(exception, "Failed to load goals.") }
        }
    }

    fun observeGoals() {
        goalsJob?.cancel()
        goalsJob = viewModelScope.launch {
            repository.observeGoals().collect { result ->
                result
                    .onSuccess { goals ->
                        _uiState.update { it.copy(isLoading = false, goals = goals, errorMessage = null).withDerivedGoals() }
                    }
                    .onFailure { exception -> showFailure(exception, "Failed to observe goals.") }
            }
        }
    }

    fun updateProgress(unit: GoalUnit, amount: Int) {
        viewModelScope.launch {
            repository.updateProgress(unit, amount)
                .onFailure { exception -> showFailure(exception, "Failed to update goal progress.") }
        }
    }

    fun clear() {
        goalsJob?.cancel()
        goalsJob = null
        _uiState.value = GoalUiState()
    }

    private fun GoalUiState.withDerivedGoals(): GoalUiState {
        return copy(
            dailyGoals = goals.filter { it.goalType == GoalType.DAILY },
            weeklyGoals = goals.filter { it.goalType == GoalType.WEEKLY },
            monthlyGoals = goals.filter { it.goalType == GoalType.MONTHLY },
            activeGoals = goals.filter { !it.completed },
            completedGoals = goals.filter { it.completed }
        )
    }

    private fun showFailure(exception: Throwable, fallback: String) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = exception.message ?: fallback, successMessage = null)
        }
    }
}

class GoalViewModelFactory(
    private val repository: GoalRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            return GoalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
