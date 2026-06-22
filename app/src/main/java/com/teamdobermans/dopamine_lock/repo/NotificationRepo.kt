package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun savePreferences(preferences: NotificationPreferences): Result<Unit>

    fun getPreferences(): Flow<NotificationPreferences>

    suspend fun scheduleWorkers(): Result<Unit>

    suspend fun cancelWorkers(): Result<Unit>

    suspend fun refreshWorkers(): Result<Unit>

    suspend fun scheduleMissionReminder(missionId: String): Result<Unit>

    suspend fun cancelMissionReminder(missionId: String): Result<Unit>

    fun showMissionCompletedNotification(title: String)

    fun showMilestoneNotification(streak: Int)
}
