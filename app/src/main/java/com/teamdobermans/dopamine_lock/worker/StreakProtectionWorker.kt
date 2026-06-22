package com.teamdobermans.dopamine_lock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.dopamine_lock.model.NotificationType
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first

class StreakProtectionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val preferences = NotificationRepositoryImpl(applicationContext).getPreferences().first()
        if (!preferences.streakReminderEnabled) return Result.success()
        if (!NotificationWorkerDataSource.hasActiveStreak()) return Result.success()
        if (NotificationWorkerDataSource.hasCompletedMissionToday()) return Result.success()

        DopamineNotificationManager(applicationContext).showNotification(
            id = NOTIFICATION_ID,
            type = NotificationType.STREAK_PROTECTION,
            title = "Protect Your Streak",
            body = "One mission today keeps your discipline chain alive."
        )
        Result.success()
    }.getOrElse { Result.retry() }

    private companion object {
        const val NOTIFICATION_ID = 2001
    }
}
