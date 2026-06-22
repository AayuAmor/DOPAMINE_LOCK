package com.teamdobermans.dopamine_lock.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.teamdobermans.dopamine_lock.MainActivity
import com.teamdobermans.dopamine_lock.R
import com.teamdobermans.dopamine_lock.enforcement.UsageStatsMonitor
import com.teamdobermans.dopamine_lock.model.MissionEnforcementState
import com.teamdobermans.dopamine_lock.notification.NotificationChannels
import com.teamdobermans.dopamine_lock.repo.EnforcementRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MissionEnforcementService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: EnforcementRepositoryImpl
    private lateinit var usageStatsMonitor: UsageStatsMonitor
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        repository = EnforcementRepositoryImpl(applicationContext)
        usageStatsMonitor = UsageStatsMonitor(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
        monitorMission()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        monitorJob?.cancel()
        super.onDestroy()
    }

    private fun startInForeground() {
        val notification = buildNotification(MissionEnforcementState())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun monitorMission() {
        if (monitorJob?.isActive == true) return

        monitorJob = serviceScope.launch {
            while (isActive) {
                val state = repository.observeEnforcementState().first()
                val settings = repository.observeSettings().first()
                if (!state.active || !settings.blockingEnabled) {
                    stopSelf()
                    return@launch
                }

                updateNotification(state)
                usageStatsMonitor.getCurrentForegroundPackage()?.let { packageName ->
                    DopamineAccessibilityService.enforcePackage(applicationContext, packageName)
                }
                delay(MONITOR_INTERVAL_MS)
            }
        }
    }

    private fun updateNotification(state: MissionEnforcementState) {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: MissionEnforcementState): Notification {
        val title = state.missionTitle.ifBlank { "MISSION ACTIVE" }
        val remainingText = remainingText(state)
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            intent.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_ENFORCEMENT_DESTINATION, "mission")
            },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationChannels.ENFORCEMENT_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(remainingText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$remainingText\nReturn to your mission."))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun remainingText(state: MissionEnforcementState): String {
        if (state.startedAt <= 0L || state.durationMinutes <= 0) return "Return to your mission."
        val elapsedMs = System.currentTimeMillis() - state.startedAt
        val remainingMs = (state.durationMinutes * 60_000L - elapsedMs).coerceAtLeast(0L)
        val remainingMinutes = (remainingMs / 60_000L).coerceAtLeast(0L)
        return "$remainingMinutes minutes remaining"
    }

    companion object {
        const val NOTIFICATION_ID = 9101
        const val EXTRA_ENFORCEMENT_DESTINATION = "enforcement_destination"
        private const val MONITOR_INTERVAL_MS = 1_500L

        fun startIntent(context: Context): Intent {
            return Intent(context, MissionEnforcementService::class.java)
        }
    }
}
