package com.teamdobermans.dopamine_lock.model

data class MissionEnforcementState(
    val missionId: String = "",
    val missionTitle: String = "",
    val active: Boolean = false,
    val blockedApps: List<String> = emptyList(),
    val startedAt: Long = 0L,
    val durationMinutes: Int = 0,
    val lastBlockedPackage: String = "",
    val blockedAttempts: Int = 0
)
