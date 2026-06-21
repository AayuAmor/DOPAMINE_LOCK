package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.data.repository.FocusSessionRepository
import com.teamdobermans.dopamine_lock.domain.model.FocusSession
import com.teamdobermans.dopamine_lock.util.SessionStatsCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusSessionUiState(
    val isLoading: Boolean = false,
    val activeSession: FocusSession? = null,
    val sessions: List<FocusSession> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalFocusHours: Double = 0.0,
    val completedSessions: Int = 0,
    val failedSessions: Int = 0,
    val successRate: Int = 0,
    val todayFocusHours: Double = 0.0,
    val todaySessionCount: Int = 0,
    val weeklyFocusHours: List<Float> = List(7) { 0f }
)

class FocusSessionViewModel(
    private val repository: FocusSessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusSessionUiState())
    val uiState: StateFlow<FocusSessionUiState> = _uiState.asStateFlow()

    private var sessionsJob: Job? = null
    private var activeSessionJob: Job? = null

    fun startSession(
        missionName: String,
        missionGoal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>,
        onSuccess: (FocusSession) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.startSession(missionName, missionGoal, missionType, durationMinutes, blockedApps)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(isLoading = false, activeSession = session, successMessage = "Session started.")
                    }
                    onSuccess(session)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to start session.")
                    }
                }
        }
    }

    fun completeSession(sessionId: String, elapsedSeconds: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.completeSession(sessionId, elapsedSeconds)
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(isLoading = false, activeSession = null, successMessage = "Session completed.")
                    }
                    loadSessions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to complete session.")
                    }
                }
        }
    }

    fun abandonSession(sessionId: String, elapsedSeconds: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.abandonSession(sessionId, elapsedSeconds)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, activeSession = null, successMessage = "Session ended.")
                    }
                    loadSessions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to abandon session.")
                    }
                }
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCurrentUserSessions()
                .onSuccess { sessions -> applySessions(sessions) }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load sessions.")
                    }
                }
        }
    }

    fun observeSessions() {
        sessionsJob?.cancel()
        sessionsJob = viewModelScope.launch {
            repository.observeCurrentUserSessions().collect { result ->
                result
                    .onSuccess { sessions -> applySessions(sessions) }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(errorMessage = exception.message ?: "Failed to load sessions.")
                        }
                    }
            }
        }
    }

    fun observeActiveSession() {
        activeSessionJob?.cancel()
        activeSessionJob = viewModelScope.launch {
            repository.observeActiveSession().collect { result ->
                result
                    .onSuccess { session ->
                        _uiState.update { it.copy(activeSession = session) }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(errorMessage = exception.message ?: "Failed to load active session.")
                        }
                    }
            }
        }
    }

    fun loadSessionsByDateRange(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getSessionsByDateRange(startTime, endTime)
                .onSuccess { sessions -> applySessions(sessions) }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load sessions.")
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clear() {
        sessionsJob?.cancel()
        activeSessionJob?.cancel()
        sessionsJob = null
        activeSessionJob = null
        _uiState.value = FocusSessionUiState()
    }

    private fun applySessions(sessions: List<FocusSession>) {
        _uiState.update {
            it.copy(
                isLoading = false,
                sessions = sessions,
                totalFocusHours = SessionStatsCalculator.totalFocusHours(sessions),
                completedSessions = SessionStatsCalculator.completedSessions(sessions),
                failedSessions = SessionStatsCalculator.failedSessions(sessions),
                successRate = SessionStatsCalculator.successRate(sessions),
                todayFocusHours = SessionStatsCalculator.todayFocusHours(sessions),
                todaySessionCount = SessionStatsCalculator.todaySessionCount(sessions),
                weeklyFocusHours = SessionStatsCalculator.weeklyFocusHours(sessions)
            )
        }
    }
}

class FocusSessionViewModelFactory(
    private val repository: FocusSessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusSessionViewModel::class.java)) {
            return FocusSessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
