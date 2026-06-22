package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.Mission

data class MissionUiState(
    val isLoading: Boolean = false,
    val activeMission: Mission? = null,
    val missions: List<Mission> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)
