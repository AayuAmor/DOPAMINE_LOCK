package com.teamdobermans.dopamine_lock.model

data class BlockedApp(
    val packageName: String = "",
    val appName: String = "",
    val enabled: Boolean = true
)
