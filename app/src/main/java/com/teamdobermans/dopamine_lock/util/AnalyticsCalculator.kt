package com.teamdobermans.dopamine_lock.util

import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.model.MissionStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AnalyticsCalculator {
    fun totalFocusHours(sessions: List<FocusSession>): Double {
        return completedSessionsList(sessions).sumOf { it.elapsedSeconds } / SECONDS_PER_HOUR
    }

    fun completedSessions(sessions: List<FocusSession>): Int {
        return sessions.count { it.completed }
    }

    fun failedSessions(sessions: List<FocusSession>): Int {
        return sessions.count { it.abandoned }
    }

    fun successRate(sessions: List<FocusSession>): Int {
        if (sessions.isEmpty()) return 0
        return ((completedSessions(sessions).toFloat() / sessions.size.toFloat()) * 100).toInt()
    }

    fun bestFocusDay(sessions: List<FocusSession>): String {
        val bestDay = completedSessionsList(sessions)
            .groupBy { dayKey(it.endedAt.takeIf { endedAt -> endedAt > 0L } ?: it.startedAt) }
            .maxByOrNull { entry -> entry.value.sumOf { it.elapsedSeconds } }
            ?.key

        return bestDay?.let { dayLabel(it) }.orEmpty()
    }

    fun bestFocusDayHours(sessions: List<FocusSession>): Double {
        return completedSessionsList(sessions)
            .groupBy { dayKey(it.endedAt.takeIf { endedAt -> endedAt > 0L } ?: it.startedAt) }
            .maxOfOrNull { entry -> entry.value.sumOf { it.elapsedSeconds } / SECONDS_PER_HOUR }
            ?: 0.0
    }

    fun focusDistribution(sessions: List<FocusSession>): Map<String, Double> {
        return completedSessionsList(sessions)
            .groupBy { it.missionType.ifBlank { "Focus" } }
            .mapValues { entry -> entry.value.sumOf { it.elapsedSeconds } / SECONDS_PER_HOUR }
    }

    fun weeklyFocusHours(sessions: List<FocusSession>, now: Long = System.currentTimeMillis()): List<Double> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            setStartOfDay()
        }
        val weekStart = calendar.timeInMillis

        return List(7) { index ->
            val dayStart = weekStart + index * DAY_MS
            val dayEnd = dayStart + DAY_MS
            completedSessionsList(sessions)
                .filter { sessionTimestamp(it) in dayStart until dayEnd }
                .sumOf { it.elapsedSeconds } / SECONDS_PER_HOUR
        }
    }

    fun monthlyFocusHours(sessions: List<FocusSession>, now: Long = System.currentTimeMillis()): List<Double> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            setStartOfDay()
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthStart = calendar.timeInMillis

        return List(daysInMonth) { index ->
            val dayStart = monthStart + index * DAY_MS
            val dayEnd = dayStart + DAY_MS
            completedSessionsList(sessions)
                .filter { sessionTimestamp(it) in dayStart until dayEnd }
                .sumOf { it.elapsedSeconds } / SECONDS_PER_HOUR
        }
    }

    fun goalCompletionRate(goals: List<Goal>): Int {
        if (goals.isEmpty()) return 0
        return ((goals.count { it.completed }.toFloat() / goals.size.toFloat()) * 100).toInt()
    }

    fun disciplineGrowth(events: List<DisciplineEvent>, currentScore: Int, now: Long = System.currentTimeMillis()): Int {
        val sevenDaysAgo = now - 7 * DAY_MS
        val recentPoints = events.filter { it.createdAt >= sevenDaysAgo }.sumOf { it.points }
        val previousScore = (currentScore - recentPoints).coerceAtLeast(0)
        return currentScore - previousScore
    }

    fun mostProductiveMissionType(sessions: List<FocusSession>): String {
        return focusDistribution(sessions).maxByOrNull { it.value }?.key.orEmpty()
    }

    fun averageSessionLengthMinutes(sessions: List<FocusSession>): Int {
        val completed = completedSessionsList(sessions)
        if (completed.isEmpty()) return 0
        return (completed.sumOf { it.elapsedSeconds } / completed.size / SECONDS_PER_MINUTE).toInt()
    }

    fun missionCompletionRate(missions: List<Mission>): Int {
        if (missions.isEmpty()) return 0
        return ((missions.count { it.status == MissionStatus.COMPLETED }.toFloat() / missions.size.toFloat()) * 100).toInt()
    }

    private fun completedSessionsList(sessions: List<FocusSession>): List<FocusSession> {
        return sessions.filter { it.completed }
    }

    private fun sessionTimestamp(session: FocusSession): Long {
        return session.endedAt.takeIf { it > 0L } ?: session.startedAt
    }

    private fun dayKey(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            setStartOfDay()
        }.timeInMillis
    }

    private fun dayLabel(dayStart: Long): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(dayStart)
    }

    private fun Calendar.setStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private const val SECONDS_PER_MINUTE = 60.0
    private const val SECONDS_PER_HOUR = 3600.0
    private const val DAY_MS = 24 * 60 * 60 * 1000L
}
