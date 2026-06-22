package com.teamdobermans.dopamine_lock.util

import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.GoalType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object GoalResetManager {
    fun createGoalWindow(goalType: GoalType, now: Long = System.currentTimeMillis(), zoneId: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
        return periodWindow(goalType, Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate(), zoneId)
    }

    fun resetIfExpired(goal: Goal, now: Long = System.currentTimeMillis(), zoneId: ZoneId = ZoneId.systemDefault()): Goal {
        if (goal.endDate <= 0L || now <= goal.endDate) return goal

        val (startDate, endDate) = createGoalWindow(goal.goalType, now, zoneId)
        return goal.copy(
            currentProgress = 0,
            completed = false,
            startDate = startDate,
            endDate = endDate,
            completedAt = 0L
        )
    }

    private fun periodWindow(goalType: GoalType, date: LocalDate, zoneId: ZoneId): Pair<Long, Long> {
        val startDate = when (goalType) {
            GoalType.DAILY -> date
            GoalType.WEEKLY -> date.with(DayOfWeek.MONDAY)
            GoalType.MONTHLY -> date.withDayOfMonth(1)
        }
        val endExclusiveDate = when (goalType) {
            GoalType.DAILY -> startDate.plusDays(1)
            GoalType.WEEKLY -> startDate.plusWeeks(1)
            GoalType.MONTHLY -> startDate.plusMonths(1)
        }

        val start = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = endExclusiveDate.atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
        return start to end
    }
}
