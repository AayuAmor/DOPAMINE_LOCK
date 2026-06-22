package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.EnforcementPermissionStatus
import com.teamdobermans.dopamine_lock.model.EnforcementSettings
import com.teamdobermans.dopamine_lock.model.MissionEnforcementState

data class EnforcementUiState(
    val isLoading: Boolean = false,
    val isEnforcementActive: Boolean = false,
    val blockedApps: List<String> = emptyList(),
    val activeMission: MissionEnforcementState = MissionEnforcementState(),
    val settings: EnforcementSettings = EnforcementSettings(),
    val permissionStatus: EnforcementPermissionStatus = EnforcementPermissionStatus(),
    val errorMessage: String? = null
)
