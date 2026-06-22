package com.teamdobermans.dopamine_lock.enforcement

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class UsageStatsMonitor(
    context: Context
) {
    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

    fun getCurrentForegroundPackage(lookbackMillis: Long = DEFAULT_LOOKBACK_MS): String? {
        val endTime = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(endTime - lookbackMillis, endTime)
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> foregroundPackage = event.packageName
            }
        }

        return foregroundPackage
    }

    private companion object {
        const val DEFAULT_LOOKBACK_MS = 10_000L
    }
}
