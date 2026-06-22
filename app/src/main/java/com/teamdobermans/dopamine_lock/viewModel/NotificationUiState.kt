package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.NotificationPreferences

data class NotificationUiState(
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
