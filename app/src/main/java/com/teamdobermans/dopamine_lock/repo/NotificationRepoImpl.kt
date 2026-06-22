package com.teamdobermans.dopamine_lock.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.workDataOf
import androidx.work.WorkManager
import com.teamdobermans.dopamine_lock.model.NotificationPreferences
import com.teamdobermans.dopamine_lock.model.NotificationType
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.worker.DailyGoalReminderWorker
import com.teamdobermans.dopamine_lock.worker.GoalProgressWorker
import com.teamdobermans.dopamine_lock.worker.MilestoneWorker
import com.teamdobermans.dopamine_lock.worker.MissionReminderWorker
import com.teamdobermans.dopamine_lock.worker.StreakProtectionWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_preferences"
)

class NotificationRepositoryImpl(
    private val context: Context
) : NotificationRepository {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)
    private val notificationManager = DopamineNotificationManager(appContext)

    override suspend fun savePreferences(preferences: NotificationPreferences): Result<Unit> = runCatchingNotification {
        appContext.notificationDataStore.edit { store ->
            store[DAILY_GOAL_REMINDER_ENABLED] = preferences.dailyGoalReminderEnabled
            store[STREAK_REMINDER_ENABLED] = preferences.streakReminderEnabled
            store[GOAL_REMINDER_ENABLED] = preferences.goalReminderEnabled
            store[MISSION_REMINDER_ENABLED] = preferences.missionReminderEnabled
            store[MILESTONE_NOTIFICATIONS_ENABLED] = preferences.milestoneNotificationsEnabled
        }
        refreshWorkers().getOrThrow()
    }

    override fun getPreferences(): Flow<NotificationPreferences> {
        return appContext.notificationDataStore.data.map { store ->
            NotificationPreferences(
                dailyGoalReminderEnabled = store[DAILY_GOAL_REMINDER_ENABLED] ?: true,
                streakReminderEnabled = store[STREAK_REMINDER_ENABLED] ?: true,
                goalReminderEnabled = store[GOAL_REMINDER_ENABLED] ?: true,
                missionReminderEnabled = store[MISSION_REMINDER_ENABLED] ?: true,
                milestoneNotificationsEnabled = store[MILESTONE_NOTIFICATIONS_ENABLED] ?: true
            )
        }
    }

    override suspend fun scheduleWorkers(): Result<Unit> = runCatchingNotification {
        notificationManager.createChannels()
        val preferences = getPreferences().first()

        if (preferences.dailyGoalReminderEnabled) {
            scheduleDailyWorker<DailyGoalReminderWorker>(
                name = DAILY_GOAL_WORK,
                hour = 8,
                minute = 0
            )
        }
        if (preferences.streakReminderEnabled) {
            scheduleDailyWorker<StreakProtectionWorker>(
                name = STREAK_PROTECTION_WORK,
                hour = 20,
                minute = 0
            )
        }
        if (preferences.goalReminderEnabled) {
            scheduleDailyWorker<GoalProgressWorker>(
                name = GOAL_PROGRESS_WORK,
                hour = 18,
                minute = 0
            )
        }
        if (preferences.milestoneNotificationsEnabled) {
            scheduleDailyWorker<MilestoneWorker>(
                name = MILESTONE_WORK,
                hour = 9,
                minute = 0
            )
        }

        if (!preferences.dailyGoalReminderEnabled) workManager.cancelUniqueWork(DAILY_GOAL_WORK)
        if (!preferences.streakReminderEnabled) workManager.cancelUniqueWork(STREAK_PROTECTION_WORK)
        if (!preferences.goalReminderEnabled) workManager.cancelUniqueWork(GOAL_PROGRESS_WORK)
        if (!preferences.milestoneNotificationsEnabled) workManager.cancelUniqueWork(MILESTONE_WORK)
    }

    override suspend fun cancelWorkers(): Result<Unit> = runCatchingNotification {
        listOf(
            DAILY_GOAL_WORK,
            STREAK_PROTECTION_WORK,
            GOAL_PROGRESS_WORK,
            MILESTONE_WORK,
            MISSION_REMINDER_WORK
        ).forEach(workManager::cancelUniqueWork)
    }

    override suspend fun refreshWorkers(): Result<Unit> = runCatchingNotification {
        scheduleWorkers().getOrThrow()
    }

    override suspend fun scheduleMissionReminder(missionId: String): Result<Unit> = runCatchingNotification {
        val preferences = getPreferences().first()
        if (!preferences.missionReminderEnabled || missionId.isBlank()) return@runCatchingNotification

        val request = OneTimeWorkRequestBuilder<MissionReminderWorker>()
            .setInitialDelay(30, TimeUnit.MINUTES)
            .setInputData(workDataOf(MissionReminderWorker.KEY_MISSION_ID to missionId))
            .build()

        workManager.enqueueUniqueWork(
            "$MISSION_REMINDER_WORK-$missionId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override suspend fun cancelMissionReminder(missionId: String): Result<Unit> = runCatchingNotification {
        if (missionId.isNotBlank()) {
            workManager.cancelUniqueWork("$MISSION_REMINDER_WORK-$missionId")
        }
    }

    override fun showMissionCompletedNotification(title: String) {
        notificationManager.showNotification(
            id = MISSION_COMPLETED_NOTIFICATION_ID,
            type = NotificationType.MISSION_COMPLETED,
            title = "Mission Completed",
            body = title.ifBlank { "You kept your promise to yourself." }
        )
    }

    override fun showMilestoneNotification(streak: Int) {
        notificationManager.showNotification(
            id = STREAK_MILESTONE_NOTIFICATION_ID + streak,
            type = NotificationType.STREAK_MILESTONE,
            title = "$streak Day Streak",
            body = "Discipline is becoming your identity."
        )
    }

    private inline fun <reified T : androidx.work.ListenableWorker> scheduleDailyWorker(
        name: String,
        hour: Int,
        minute: Int
    ) {
        val request = PeriodicWorkRequestBuilder<T>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(hour, minute).toMillis(), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            name,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun delayUntil(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var target = now.with(LocalTime.of(hour, minute))
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target)
    }

    private suspend fun <T> runCatchingNotification(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(NotificationException(exception.message ?: "Notification request failed."))
        }
    }

    private class NotificationException(message: String) : Exception(message)

    private companion object {
        const val DAILY_GOAL_WORK = "daily_goal_reminder_work"
        const val STREAK_PROTECTION_WORK = "streak_protection_work"
        const val GOAL_PROGRESS_WORK = "goal_progress_work"
        const val MILESTONE_WORK = "milestone_work"
        const val MISSION_REMINDER_WORK = "mission_reminder_work"

        const val MISSION_COMPLETED_NOTIFICATION_ID = 5001
        const val STREAK_MILESTONE_NOTIFICATION_ID = 6000

        val DAILY_GOAL_REMINDER_ENABLED = booleanPreferencesKey("daily_goal_reminder_enabled")
        val STREAK_REMINDER_ENABLED = booleanPreferencesKey("streak_reminder_enabled")
        val GOAL_REMINDER_ENABLED = booleanPreferencesKey("goal_reminder_enabled")
        val MISSION_REMINDER_ENABLED = booleanPreferencesKey("mission_reminder_enabled")
        val MILESTONE_NOTIFICATIONS_ENABLED = booleanPreferencesKey("milestone_notifications_enabled")
    }
}
