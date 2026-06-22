package com.teamdobermans.dopamine_lock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first

class MilestoneWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val repository = NotificationRepositoryImpl(applicationContext)
        val preferences = repository.getPreferences().first()
        if (!preferences.milestoneNotificationsEnabled) return Result.success()

        val streak = NotificationWorkerDataSource.currentStreak()
        if (streak in milestones) {
            repository.showMilestoneNotification(streak)
        }
        Result.success()
    }.getOrElse { Result.retry() }

    private companion object {
        val milestones = setOf(7, 14, 30, 60, 100)
    }
}
