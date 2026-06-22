package com.teamdobermans.dopamine_lock.model

data class FocusSession(
    val sessionId: String = "",
    val userId: String = "",
    val missionName: String = "",
    val missionGoal: String = "",
    val missionType: String = "",
    val durationMinutes: Int = 25,
    val completed: Boolean = false,
    val abandoned: Boolean = false,
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val blockedApps: List<String> = emptyList(),
    val disciplineXp: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
