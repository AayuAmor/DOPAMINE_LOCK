package com.teamdobermans.dopamine_lock.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teamdobermans.dopamine_lock.model.NotificationType
import com.teamdobermans.dopamine_lock.notification.DopamineNotificationManager
import com.teamdobermans.dopamine_lock.repo.NotificationRepositoryImpl
import kotlinx.coroutines.flow.first

class MissionReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = runCatching {
        val preferences = NotificationRepositoryImpl(applicationContext).getPreferences().first()
        if (!preferences.missionReminderEnabled) return Result.success()

        val missionId = inputData.getString(KEY_MISSION_ID)
        if (!NotificationWorkerDataSource.hasCreatedMissionNotStarted(missionId)) return Result.success()

        DopamineNotificationManager(applicationContext).showNotification(
            id = NOTIFICATION_ID,
            type = NotificationType.MISSION_REMINDER,
            title = "Mission Waiting",
            body = "Your mission is ready. Start now."
        )
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        const val KEY_MISSION_ID = "mission_id"
        const val NOTIFICATION_ID = 4001
    }
}
