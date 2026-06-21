package com.teamdobermans.dopamine_lock.domain.model

data class Mission(
    val missionId: String = "",
    val userId: String = "",
    val title: String = "",
    val goal: String = "",
    val missionType: String = "",
    val durationMinutes: Int = 25,
    val blockedApps: List<String> = emptyList(),
    val status: MissionStatus = MissionStatus.CREATED,
    val createdAt: Long = 0L,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val disciplineReward: Int = 0,
    val disciplinePenalty: Int = 0
)
