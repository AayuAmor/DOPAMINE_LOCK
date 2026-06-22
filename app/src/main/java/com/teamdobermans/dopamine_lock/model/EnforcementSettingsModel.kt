package com.teamdobermans.dopamine_lock.model

data class EnforcementSettings(
    val strictModeEnabled: Boolean = false,
    val blockingEnabled: Boolean = true,
    val overlayProtectionEnabled: Boolean = true,
    val foregroundServiceEnabled: Boolean = true
)
