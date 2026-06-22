package com.teamdobermans.dopamine_lock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.dopamine_lock.model.NotificationType
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first

class DailyGoalReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val preferences = NotificationRepositoryImpl(applicationContext).getPreferences().first()
        if (!preferences.dailyGoalReminderEnabled) return Result.success()
        if (NotificationWorkerDataSource.hasCompletedSessionToday()) return Result.success()

        DopamineNotificationManager(applicationContext).showNotification(
            id = NOTIFICATION_ID,
            type = NotificationType.DAILY_GOAL_REMINDER,
            title = "Today's Mission Awaits",
            body = "You haven't started today's focus journey yet."
        )
        Result.success()
    }.getOrElse { Result.retry() }

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
