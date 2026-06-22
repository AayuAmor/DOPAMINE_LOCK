package com.teamdobermans.dopamine_lock.model

data class DisciplineEvent(
    val eventId: String = "",
    val userId: String = "",
    val eventType: String = "",
    val points: Int = 0,
    val description: String = "",
    val relatedMissionId: String? = null,
    val relatedSessionId: String? = null,
    val createdAt: Long = 0L
)
