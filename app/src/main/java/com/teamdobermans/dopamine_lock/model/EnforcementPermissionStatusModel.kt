package com.teamdobermans.dopamine_lock.model

data class EnforcementPermissionStatus(
    val accessibilityEnabled: Boolean = false,
    val usageAccessEnabled: Boolean = false,
    val notificationEnabled: Boolean = false
) {
    val readyForEnforcement: Boolean
        get() = accessibilityEnabled && notificationEnabled
}
