package com.teamdobermans.dopamine_lock.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.teamdobermans.dopamine_lock.MainActivity
import com.teamdobermans.dopamine_lock.R
import com.teamdobermans.dopamine_lock.model.NotificationType

class DopamineNotificationManager(
    private val context: Context
) {
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                NotificationChannels.DISCIPLINE_CHANNEL,
                "Discipline",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.GOALS_CHANNEL,
                "Goals",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.MISSIONS_CHANNEL,
                "Missions",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.STREAKS_CHANNEL,
                "Streaks",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationChannels.ENFORCEMENT_CHANNEL,
                "Mission Enforcement",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        context.getSystemService(NotificationManager::class.java).createNotificationChannels(channels)
    }

    fun showNotification(
        id: Int,
        type: NotificationType,
        title: String,
        body: String
    ): Boolean {
        if (!canNotify()) return false

        val notification = NotificationCompat.Builder(context, type.channelId())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(clickIntent(type))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
        return true
    }

    fun cancelNotification(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    private fun clickIntent(type: NotificationType): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_DESTINATION, type.destination())
        }

        return PendingIntent.getActivity(
            context,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun NotificationType.channelId(): String {
        return when (this) {
            NotificationType.DAILY_GOAL_REMINDER -> NotificationChannels.DISCIPLINE_CHANNEL
            NotificationType.STREAK_PROTECTION,
            NotificationType.STREAK_MILESTONE -> NotificationChannels.STREAKS_CHANNEL
            NotificationType.MISSION_REMINDER,
            NotificationType.MISSION_COMPLETED -> NotificationChannels.MISSIONS_CHANNEL
            NotificationType.GOAL_PROGRESS -> NotificationChannels.GOALS_CHANNEL
        }
    }

    private fun NotificationType.destination(): String {
        return when (this) {
            NotificationType.DAILY_GOAL_REMINDER -> "dashboard"
            NotificationType.STREAK_PROTECTION -> "create_mission"
            NotificationType.GOAL_PROGRESS -> "goal_tracking"
            NotificationType.MISSION_REMINDER -> "mission"
            NotificationType.MISSION_COMPLETED,
            NotificationType.STREAK_MILESTONE -> "discipline_score"
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_DESTINATION = "notification_destination"
    }
}
