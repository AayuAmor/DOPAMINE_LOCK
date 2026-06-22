package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.enforcement.PermissionManager
import com.teamdobermans.dopamine_lock.model.EnforcementSettings
import com.teamdobermans.dopamine_lock.repo.EnforcementRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnforcementViewModel(
    private val repository: EnforcementRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnforcementUiState())
    val uiState: StateFlow<EnforcementUiState> = _uiState.asStateFlow()

    private var stateJob: Job? = null
    private var settingsJob: Job? = null

    init {
        observeEnforcementState()
        observeSettings()
        checkPermissions()
    }

    fun startEnforcement(
        missionId: String,
        missionTitle: String,
        blockedApps: List<String>,
        startedAt: Long,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            repository.startEnforcement(missionId, missionTitle, blockedApps, startedAt, durationMinutes)
                .onFailure { exception ->
                    _uiState.update { it.copy(errorMessage = exception.message ?: "Failed to start enforcement.") }
                }
        }
    }

    fun stopEnforcement() {
        viewModelScope.launch {
            repository.stopEnforcement()
                .onFailure { exception ->
                    _uiState.update { it.copy(errorMessage = exception.message ?: "Failed to stop enforcement.") }
                }
        }
    }

    fun loadBlockedApps() {
        viewModelScope.launch {
            val apps = repository.getBlockedApps()
            _uiState.update { it.copy(blockedApps = apps) }
        }
    }

    fun checkPermissions() {
        _uiState.update {
            it.copy(permissionStatus = permissionManager.getPermissionStatus())
        }
    }

    fun updateSettings(settings: EnforcementSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
                .onFailure { exception ->
                    _uiState.update { it.copy(errorMessage = exception.message ?: "Failed to save enforcement settings.") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeEnforcementState() {
        stateJob?.cancel()
        stateJob = viewModelScope.launch {
            repository.observeEnforcementState().collect { state ->
                _uiState.update {
                    it.copy(
                        activeMission = state,
                        isEnforcementActive = state.active,
                        blockedApps = state.blockedApps
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = viewModelScope.launch {
            repository.observeSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }
}

class EnforcementViewModelFactory(
    private val repository: EnforcementRepository,
    private val permissionManager: PermissionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnforcementViewModel::class.java)) {
            return EnforcementViewModel(repository, permissionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
