package com.teamdobermans.dopamine_lock.viewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.data.repository.AuthRepository
import com.teamdobermans.dopamine_lock.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val hasCheckedAuthState: Boolean = false,
    val loadingProvider: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        val validationError = validateRegistration(name, email, password, confirmPassword)
        if (validationError != null) {
            showError(validationError)
            return
        }

        runAuthAction {
            authRepository.register(name, email, password)
        }
    }

    fun login(email: String, password: String) {
        val validationError = validateLogin(email, password)
        if (validationError != null) {
            showError(validationError)
            return
        }

        runAuthAction {
            authRepository.login(email, password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.logout()
                .onSuccess {
                    _uiState.value = AuthUiState(isAuthenticated = false, hasCheckedAuthState = true)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Unable to log out.")
                    }
                }
        }
    }

    fun forgotPassword(email: String) {
        val validationError = validateEmail(email)
        if (validationError != null) {
            showError(validationError)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Password reset link sent. Check your email."
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Unable to send reset email.")
                    }
                }
        }
    }

    fun googleSignIn(idToken: String) {
        runAuthAction {
            authRepository.signInWithGoogle(idToken)
        }
    }

    fun startOAuthLoading(provider: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingProvider = provider,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun githubSignIn() {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadingProvider = null,
                errorMessage = null,
                successMessage = "GitHub sign-in coming soon"
            )
        }
    }

    fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentFirebaseUser()
            if (currentUser == null) {
                _uiState.value = AuthUiState(isAuthenticated = false, hasCheckedAuthState = true)
                return@launch
            }

            _uiState.value = AuthUiState(
                isAuthenticated = true,
                user = User(
                    uid = currentUser.uid,
                    name = currentUser.displayName.orEmpty(),
                    email = currentUser.email.orEmpty()
                ),
                hasCheckedAuthState = true
            )
        }
    }

    fun setError(message: String) {
        showError(message)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, loadingProvider = null, isLoading = false) }
    }

    private fun runAuthAction(action: suspend () -> Result<User>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            action()
                .onSuccess { user ->
                    _uiState.value = AuthUiState(isAuthenticated = true, user = user, hasCheckedAuthState = true)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingProvider = null,
                            errorMessage = exception.message ?: "Authentication failed."
                        )
                    }
                }
        }
    }

    private fun validateRegistration(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (name.trim().isEmpty()) return "Name is required."
        validateLogin(email, password)?.let { return it }
        if (confirmPassword.isBlank()) return "Confirm your password."
        if (password != confirmPassword) return "Passwords do not match."
        return null
    }

    private fun validateLogin(email: String, password: String): String? {
        validateEmail(email)?.let { return it }
        if (password.isBlank()) return "Password is required."
        if (password.length < 6) return "Password must be at least 6 characters."
        return null
    }

    private fun validateEmail(email: String): String? {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) return "Email is required."
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) return "Enter a valid email address."
        return null
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, loadingProvider = null, errorMessage = message, successMessage = null) }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
