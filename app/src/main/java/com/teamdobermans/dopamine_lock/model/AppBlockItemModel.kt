package com.teamdobermans.dopamine_lock.model

data class AppBlockItem(
    val id: String,
    val name: String,
    val category: String,
    val initial: String = name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
    val isBlocked: Boolean = false
)
