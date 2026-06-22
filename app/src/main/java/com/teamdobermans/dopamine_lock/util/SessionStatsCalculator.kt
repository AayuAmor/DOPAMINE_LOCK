package com.teamdobermans.dopamine_lock.util

import com.teamdobermans.dopamine_lock.model.FocusSession
import java.util.Calendar

object SessionStatsCalculator {
    fun totalFocusHours(sessions: List<FocusSession>): Double {
        return sessions.sumOf { it.elapsedSeconds } / 3600.0
    }

    fun completedSessions(sessions: List<FocusSession>): Int {
        return sessions.count { it.completed }
    }

    fun failedSessions(sessions: List<FocusSession>): Int {
        return sessions.count { it.abandoned }
    }

    fun successRate(sessions: List<FocusSession>): Int {
        val decidedSessions = sessions.count { it.completed || it.abandoned }
        if (decidedSessions == 0) return 0
        return ((completedSessions(sessions).toFloat() / decidedSessions.toFloat()) * 100).toInt()
    }

    fun todayFocusHours(sessions: List<FocusSession>, now: Long = System.currentTimeMillis()): Double {
        return totalFocusHours(sessions.filter { isSameDay(it.startedAt, now) })
    }

    fun todaySessionCount(sessions: List<FocusSession>, now: Long = System.currentTimeMillis()): Int {
        return sessions.count { isSameDay(it.startedAt, now) }
    }

    fun recentSessions(sessions: List<FocusSession>, limit: Int = 5): List<FocusSession> {
        return sessions.sortedByDescending { it.startedAt }.take(limit)
    }

    fun weeklyFocusHours(sessions: List<FocusSession>, now: Long = System.currentTimeMillis()): List<Float> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = calendar.timeInMillis

        return List(7) { index ->
            val dayStart = weekStart + index * DAY_MS
            val dayEnd = dayStart + DAY_MS
            totalFocusHours(sessions.filter { it.startedAt in dayStart until dayEnd }).toFloat()
        }
    }

    private fun isSameDay(timestamp: Long, now: Long): Boolean {
        if (timestamp <= 0L) return false
        val first = Calendar.getInstance().apply { timeInMillis = timestamp }
        val second = Calendar.getInstance().apply { timeInMillis = now }
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    private const val DAY_MS = 24 * 60 * 60 * 1000L
}
