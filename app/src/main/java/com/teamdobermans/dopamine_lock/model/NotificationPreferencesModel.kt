package com.teamdobermans.dopamine_lock.model

data class NotificationPreferences(
    val dailyGoalReminderEnabled: Boolean = true,
    val streakReminderEnabled: Boolean = true,
    val goalReminderEnabled: Boolean = true,
    val missionReminderEnabled: Boolean = true,
    val milestoneNotificationsEnabled: Boolean = true
)
