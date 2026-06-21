package com.teamdobermans.dopamine_lock.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val disciplineScore: Int = 0
)
