package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.repo.UserRepository
import com.teamdobermans.dopamine_lock.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private var observeProfileJob: Job? = null

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            userRepository.getCurrentUserProfile()
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load profile.")
                    }
                }
        }
    }

    fun observeCurrentUserProfile() {
        observeProfileJob?.cancel()
        observeProfileJob = viewModelScope.launch {
            userRepository.ensureUserProfileExists()
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user, errorMessage = null) }
                    observeProfile(user.uid)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load profile.")
                    }
                }
        }
    }

    fun updateUserName(name: String) {
        val uid = _uiState.value.user?.uid ?: return showError("User not authenticated.")
        updateProfileField { userRepository.updateUserName(uid, name) }
    }

    fun updateUserProfile(user: User) {
        updateProfileField { userRepository.updateUserProfile(user) }
    }

    fun updateDisciplineScore(score: Int) {
        val uid = _uiState.value.user?.uid ?: return showError("User not authenticated.")
        updateProfileField { userRepository.updateDisciplineScore(uid, score) }
    }

    fun updateStreaks(currentStreak: Int, bestStreak: Int) {
        val uid = _uiState.value.user?.uid ?: return showError("User not authenticated.")
        updateProfileField { userRepository.updateStreaks(uid, currentStreak, bestStreak) }
    }

    fun updateTotalFocusHours(hours: Double) {
        val uid = _uiState.value.user?.uid ?: return showError("User not authenticated.")
        updateProfileField { userRepository.updateTotalFocusHours(uid, hours) }
    }

    fun ensureUserProfileExists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            userRepository.ensureUserProfileExists()
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load profile.")
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearUser() {
        observeProfileJob?.cancel()
        observeProfileJob = null
        _uiState.value = UserUiState()
    }

    private suspend fun observeProfile(uid: String) {
        userRepository.observeUserProfile(uid).collect { result ->
            result
                .onSuccess { user ->
                    _uiState.update { it.copy(isLoading = false, user = user, errorMessage = null) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to load profile.")
                    }
                }
        }
    }

    private fun updateProfileField(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            action()
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Profile updated.")
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Failed to update profile.")
                    }
                }
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, successMessage = null) }
    }
}

class UserViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
