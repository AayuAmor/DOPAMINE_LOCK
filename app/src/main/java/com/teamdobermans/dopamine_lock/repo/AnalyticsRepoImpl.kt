package com.teamdobermans.dopamine_lock.repo

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DatabaseException
import com.teamdobermans.dopamine_lock.model.AnalyticsSummary
import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.util.AnalyticsCalculator

class AnalyticsRepositoryImpl(
    private val focusSessionRepository: FocusSessionRepository,
    private val missionRepository: MissionRepository,
    private val goalRepository: GoalRepository,
    private val streakRepository: StreakRepository,
    private val userRepository: UserRepository,
    private val disciplineRepository: DisciplineRepository
) : AnalyticsRepository {
    override suspend fun getAnalyticsSummary(): Result<AnalyticsSummary> = runCatchingAnalytics {
        val sessions = sessions()
        val user = userRepository.getCurrentUserProfile().getOrThrow()
        val goals = goals()
        val currentStreak = streakRepository.calculateCurrentStreak().getOrDefault(user.currentStreak)
        val bestStreak = streakRepository.calculateBestStreak().getOrDefault(user.bestStreak)

        AnalyticsSummary(
            totalFocusHours = AnalyticsCalculator.totalFocusHours(sessions),
            totalSessions = sessions.size,
            completedSessions = AnalyticsCalculator.completedSessions(sessions),
            failedSessions = AnalyticsCalculator.failedSessions(sessions),
            successRate = AnalyticsCalculator.successRate(sessions),
            bestFocusDay = AnalyticsCalculator.bestFocusDay(sessions),
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            disciplineScore = user.disciplineScore,
            completedGoals = goals.count { it.completed },
            focusDistribution = AnalyticsCalculator.focusDistribution(sessions),
            weeklyFocusHours = AnalyticsCalculator.weeklyFocusHours(sessions),
            monthlyFocusHours = AnalyticsCalculator.monthlyFocusHours(sessions)
        )
    }

    override suspend fun getWeeklyAnalytics(): Result<List<Double>> = runCatchingAnalytics {
        AnalyticsCalculator.weeklyFocusHours(sessions())
    }

    override suspend fun getMonthlyAnalytics(): Result<List<Double>> = runCatchingAnalytics {
        AnalyticsCalculator.monthlyFocusHours(sessions())
    }

    override suspend fun getFocusDistribution(): Result<Map<String, Double>> = runCatchingAnalytics {
        AnalyticsCalculator.focusDistribution(sessions())
    }

    override suspend fun getSuccessRate(): Result<Int> = runCatchingAnalytics {
        AnalyticsCalculator.successRate(sessions())
    }

    override suspend fun getBestFocusDay(): Result<String> = runCatchingAnalytics {
        AnalyticsCalculator.bestFocusDay(sessions())
    }

    override suspend fun getFocusTrend(): Result<List<Double>> = getMonthlyAnalytics()

    override suspend fun getGoalStatistics(): Result<Map<String, Int>> = runCatchingAnalytics {
        val goals = goals()
        mapOf(
            "created" to goals.size,
            "completed" to goals.count { it.completed },
            "completionRate" to AnalyticsCalculator.goalCompletionRate(goals)
        )
    }

    override suspend fun getDisciplineStatistics(): Result<Map<String, Int>> = runCatchingAnalytics {
        val user = userRepository.getCurrentUserProfile().getOrThrow()
        val events = disciplineEvents()
        mapOf(
            "score" to user.disciplineScore,
            "growth" to AnalyticsCalculator.disciplineGrowth(events, user.disciplineScore),
            "eventCount" to events.size,
            "missionCompletionRate" to AnalyticsCalculator.missionCompletionRate(missions())
        )
    }

    private suspend fun sessions(): List<FocusSession> {
        return focusSessionRepository.getCurrentUserSessions().getOrThrow()
    }

    private suspend fun missions(): List<Mission> {
        return missionRepository.getCurrentUserMissions().getOrThrow()
    }

    private suspend fun goals(): List<Goal> {
        return goalRepository.getCurrentUserGoals().getOrThrow()
    }

    private suspend fun disciplineEvents(): List<DisciplineEvent> {
        return disciplineRepository.getEventHistory().getOrThrow()
    }

    private suspend fun <T> runCatchingAnalytics(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(AnalyticsException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is AnalyticsException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load analytics."
        return exception.message ?: "Analytics request failed. Please try again."
    }

    private class AnalyticsException(message: String) : Exception(message)
}
