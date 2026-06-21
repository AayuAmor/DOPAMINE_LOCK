package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.data.repository.MissionRepository
import com.teamdobermans.dopamine_lock.domain.model.Mission
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MissionViewModel(
    private val repository: MissionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    private var missionsJob: Job? = null
    private var activeMissionJob: Job? = null

    fun createMission(
        title: String,
        goal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>,
        onSuccess: (Mission) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.createMission(title, goal, missionType, durationMinutes, blockedApps)
                .onSuccess { mission ->
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Mission created.")
                    }
                    onSuccess(mission)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to create mission.")
                    }
                }
        }
    }

    fun startMission(
        missionId: String,
        onSuccess: (Mission) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.startMission(missionId)
                .onSuccess { mission ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeMission = mission,
                            successMessage = "Mission active."
                        )
                    }
                    onSuccess(mission)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to start mission.")
                    }
                }
        }
    }

    fun completeMission(missionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.completeMission(missionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, activeMission = null, successMessage = "Mission completed.")
                    }
                    loadMissions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to complete mission.")
                    }
                }
        }
    }

    fun abandonMission(missionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.abandonMission(missionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, activeMission = null, successMessage = "Mission abandoned.")
                    }
                    loadMissions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to abandon mission.")
                    }
                }
        }
    }

    fun failMission(missionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.failMission(missionId)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, activeMission = null, successMessage = "Mission failed.")
                    }
                    loadMissions()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to fail mission.")
                    }
                }
        }
    }

    fun loadMissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getCurrentUserMissions()
                .onSuccess { missions ->
                    _uiState.update { it.copy(isLoading = false, missions = missions) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load missions.")
                    }
                }
        }
    }

    fun observeMissions() {
        missionsJob?.cancel()
        missionsJob = viewModelScope.launch {
            repository.observeUserMissions().collect { result ->
                result
                    .onSuccess { missions ->
                        _uiState.update { it.copy(isLoading = false, missions = missions) }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(errorMessage = exception.message ?: "Failed to load missions.")
                        }
                    }
            }
        }
    }

    fun observeActiveMission() {
        activeMissionJob?.cancel()
        activeMissionJob = viewModelScope.launch {
            repository.observeActiveMission().collect { result ->
                result
                    .onSuccess { mission ->
                        _uiState.update { it.copy(activeMission = mission) }
                    }
                    .onFailure { exception ->
                        _uiState.update {
                            it.copy(errorMessage = exception.message ?: "Failed to load active mission.")
                        }
                    }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clear() {
        missionsJob?.cancel()
        activeMissionJob?.cancel()
        missionsJob = null
        activeMissionJob = null
        _uiState.value = MissionUiState()
    }
}

class MissionViewModelFactory(
    private val repository: MissionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MissionViewModel::class.java)) {
            return MissionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
