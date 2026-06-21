package com.teamdobermans.dopamine_lock.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val disciplineScore: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalFocusHours: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
