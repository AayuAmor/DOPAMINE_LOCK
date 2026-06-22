package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.DisciplineEventType
import com.teamdobermans.dopamine_lock.repo.DisciplineRepository
import com.teamdobermans.dopamine_lock.util.DisciplineRankCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DisciplineViewModel(
    private val repository: DisciplineRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DisciplineUiState())
    val uiState: StateFlow<DisciplineUiState> = _uiState.asStateFlow()

    private var scoreJob: Job? = null
    private var rankJob: Job? = null
    private var historyJob: Job? = null

    fun awardPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String? = null,
        relatedSessionId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.awardPoints(points, eventType, description, relatedMissionId, relatedSessionId)
                .onSuccess { score ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            score = score,
                            rank = DisciplineRankCalculator.calculateRank(score),
                            successMessage = "Discipline score updated."
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to award points.") }
        }
    }

    fun deductPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String? = null,
        relatedSessionId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.deductPoints(points, eventType, description, relatedMissionId, relatedSessionId)
                .onSuccess { score ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            score = score,
                            rank = DisciplineRankCalculator.calculateRank(score),
                            successMessage = "Discipline score updated."
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to deduct points.") }
        }
    }

    fun loadScore() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCurrentScore()
                .onSuccess { score ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            score = score,
                            rank = DisciplineRankCalculator.calculateRank(score)
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to load score.") }
        }
    }

    fun loadRank() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getRank()
                .onSuccess { rank -> _uiState.update { it.copy(isLoading = false, rank = rank) } }
                .onFailure { exception -> showFailure(exception, "Failed to load rank.") }
        }
    }

    fun observeScore() {
        scoreJob?.cancel()
        scoreJob = viewModelScope.launch {
            repository.observeScore().collect { result ->
                result
                    .onSuccess { score ->
                        _uiState.update {
                            it.copy(score = score, rank = DisciplineRankCalculator.calculateRank(score))
                        }
                    }
                    .onFailure { exception -> showFailure(exception, "Failed to observe score.") }
            }
        }
    }

    fun observeRank() {
        rankJob?.cancel()
        rankJob = viewModelScope.launch {
            repository.observeRank().collect { result ->
                result
                    .onSuccess { rank -> _uiState.update { it.copy(rank = rank) } }
                    .onFailure { exception -> showFailure(exception, "Failed to observe rank.") }
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getEventHistory()
                .onSuccess { events ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            events = events,
                            recentEvents = events.take(5)
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to load discipline history.") }
        }
    }

    fun observeHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            repository.observeEventHistory().collect { result ->
                result
                    .onSuccess { events ->
                        _uiState.update {
                            it.copy(events = events, recentEvents = events.take(5), errorMessage = null)
                        }
                    }
                    .onFailure { exception -> showFailure(exception, "Failed to observe discipline history.") }
            }
        }
    }

    fun clear() {
        scoreJob?.cancel()
        rankJob?.cancel()
        historyJob?.cancel()
        scoreJob = null
        rankJob = null
        historyJob = null
        _uiState.value = DisciplineUiState()
    }

    private fun showFailure(exception: Throwable, fallback: String) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = exception.message ?: fallback, successMessage = null)
        }
    }
}

class DisciplineViewModelFactory(
    private val repository: DisciplineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DisciplineViewModel::class.java)) {
            return DisciplineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
