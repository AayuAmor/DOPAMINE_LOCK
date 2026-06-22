package com.teamdobermans.dopamine_lock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.dopamine_lock.model.NotificationType
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first

class GoalProgressWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val preferences = NotificationRepositoryImpl(applicationContext).getPreferences().first()
        if (!preferences.goalReminderEnabled) return Result.success()
        if (!NotificationWorkerDataSource.hasIncompleteActiveGoal()) return Result.success()

        DopamineNotificationManager(applicationContext).showNotification(
            id = NOTIFICATION_ID,
            type = NotificationType.GOAL_PROGRESS,
            title = "Goal Progress Check",
            body = "You're closer than you think. Keep going."
        )
        Result.success()
    }.getOrElse { Result.retry() }

    private companion object {
        const val NOTIFICATION_ID = 3001
    }
}
