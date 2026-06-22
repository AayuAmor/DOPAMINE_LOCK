package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.NotificationPreferences
import com.teamdobermans.dopamine_lock.repo.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private var preferencesJob: Job? = null

    fun loadPreferences() {
        preferencesJob?.cancel()
        preferencesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getPreferences().collect { preferences ->
                _uiState.update {
                    it.copy(
                        preferences = preferences,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun updatePreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.savePreferences(preferences)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            preferences = preferences,
                            isLoading = false,
                            successMessage = "Notification preferences updated."
                        )
                    }
                }
                .onFailure { exception -> showFailure(exception, "Failed to update notifications.") }
        }
    }

    fun scheduleNotifications() {
        viewModelScope.launch {
            repository.scheduleWorkers()
                .onSuccess { _uiState.update { it.copy(successMessage = "Notifications scheduled.") } }
                .onFailure { exception -> showFailure(exception, "Failed to schedule notifications.") }
        }
    }

    fun cancelNotifications() {
        viewModelScope.launch {
            repository.cancelWorkers()
                .onSuccess { _uiState.update { it.copy(successMessage = "Notifications cancelled.") } }
                .onFailure { exception -> showFailure(exception, "Failed to cancel notifications.") }
        }
    }

    fun clear() {
        preferencesJob?.cancel()
        preferencesJob = null
        _uiState.value = NotificationUiState()
    }

    private fun showFailure(exception: Throwable, fallback: String) {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = exception.message ?: fallback, successMessage = null)
        }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
