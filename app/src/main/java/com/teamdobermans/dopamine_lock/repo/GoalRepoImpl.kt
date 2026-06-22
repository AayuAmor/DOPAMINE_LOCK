package com.teamdobermans.dopamine_lock.repo

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.model.DisciplineEventType
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.GoalType
import com.teamdobermans.dopamine_lock.model.GoalUnit
import com.teamdobermans.dopamine_lock.util.GoalResetManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GoalRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val disciplineRepository: DisciplineRepository? = null,
    private val notificationRepository: NotificationRepository? = null
) : GoalRepository {
    private val goalsRef: DatabaseReference = database.reference.child(GOALS_PATH)

    override suspend fun createGoal(
        title: String,
        description: String,
        goalType: GoalType,
        targetValue: Int,
        unit: GoalUnit
    ): Result<Goal> = runCatchingGoal {
        val uid = currentUid()
        if (title.trim().isEmpty()) throw GoalException("Goal title is required.")
        if (targetValue <= 0) throw GoalException("Goal target must be greater than zero.")

        val now = System.currentTimeMillis()
        val goalId = goalsRef.child(uid).push().key ?: UUID.randomUUID().toString()
        val (startDate, endDate) = GoalResetManager.createGoalWindow(goalType, now)
        val goal = Goal(
            goalId = goalId,
            userId = uid,
            title = title.trim(),
            description = description.trim(),
            goalType = goalType,
            targetValue = targetValue,
            unit = unit,
            createdAt = now,
            startDate = startDate,
            endDate = endDate
        )

        goalRef(uid, goalId).setValue(goal).await()
        notificationRepository?.refreshWorkers()
        goal
    }

    override suspend fun updateGoal(goal: Goal): Result<Goal> = runCatchingGoal {
        val uid = currentUid()
        if (goal.goalId.isBlank()) throw GoalException("Goal not found.")

        val existing = fetchGoal(uid, goal.goalId)
        val updated = goal.copy(
            goalId = existing.goalId,
            userId = uid,
            createdAt = existing.createdAt,
            completed = goal.currentProgress >= goal.targetValue,
            completedAt = if (goal.currentProgress >= goal.targetValue) {
                goal.completedAt.takeIf { it > 0L } ?: System.currentTimeMillis()
            } else {
                0L
            }
        )
        goalRef(uid, updated.goalId).setValue(updated).await()
        notificationRepository?.refreshWorkers()
        updated
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> = runCatchingGoal {
        goalRef(currentUid(), goalId).removeValue().await()
        notificationRepository?.refreshWorkers()
    }

    override suspend fun completeGoal(goalId: String): Result<Goal> = runCatchingGoal {
        val uid = currentUid()
        val goal = fetchGoal(uid, goalId)
        if (goal.completed) return@runCatchingGoal goal

        val now = System.currentTimeMillis()
        val completedGoal = goal.copy(
            currentProgress = goal.currentProgress.coerceAtLeast(goal.targetValue),
            completed = true,
            completedAt = now
        )
        goalRef(uid, goalId).setValue(completedGoal).await()
        awardGoalCompletion(completedGoal)
        notificationRepository?.refreshWorkers()
        completedGoal
    }

    override suspend fun getGoal(goalId: String): Result<Goal> = runCatchingGoal {
        val uid = currentUid()
        resetExpiredGoal(fetchGoal(uid, goalId))
    }

    override suspend fun getCurrentUserGoals(): Result<List<Goal>> = runCatchingGoal {
        resetExpiredGoals(fetchGoals(currentUid()))
    }

    override suspend fun getDailyGoals(): Result<List<Goal>> = runCatchingGoal {
        getCurrentUserGoals().getOrThrow().filter { it.goalType == GoalType.DAILY }
    }

    override suspend fun getWeeklyGoals(): Result<List<Goal>> = runCatchingGoal {
        getCurrentUserGoals().getOrThrow().filter { it.goalType == GoalType.WEEKLY }
    }

    override suspend fun getMonthlyGoals(): Result<List<Goal>> = runCatchingGoal {
        getCurrentUserGoals().getOrThrow().filter { it.goalType == GoalType.MONTHLY }
    }

    override suspend fun updateProgress(unit: GoalUnit, amount: Int): Result<List<Goal>> = runCatchingGoal {
        if (amount <= 0) return@runCatchingGoal emptyList()
        val uid = currentUid()
        val updatedGoals = mutableListOf<Goal>()

        resetExpiredGoals(fetchGoals(uid))
            .filter { !it.completed && it.unit == unit }
            .forEach { goal ->
                val updated = goal.copy(
                    currentProgress = (goal.currentProgress + amount).coerceAtMost(goal.targetValue)
                )
                val savedGoal = if (updated.currentProgress >= updated.targetValue) {
                    val completedGoal = updated.copy(completed = true, completedAt = System.currentTimeMillis())
                    goalRef(uid, completedGoal.goalId).setValue(completedGoal).await()
                    awardGoalCompletion(completedGoal)
                    notificationRepository?.refreshWorkers()
                    completedGoal
                } else {
                    goalRef(uid, updated.goalId).setValue(updated).await()
                    notificationRepository?.refreshWorkers()
                    updated
                }
                updatedGoals += savedGoal
            }

        updatedGoals
    }

    override fun observeGoals(): Flow<Result<List<Goal>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(GoalException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = goalsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goals = snapshot.toGoalList().map { GoalResetManager.resetIfExpired(it) }
                trySend(Result.success(goals))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(GoalException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeActiveGoals(): Flow<Result<List<Goal>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(GoalException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = goalsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goals = snapshot.toGoalList()
                    .map { GoalResetManager.resetIfExpired(it) }
                    .filter { !it.completed }
                trySend(Result.success(goals))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(GoalException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun resetExpiredGoals(goals: List<Goal>): List<Goal> {
        return goals.map { resetExpiredGoal(it) }
    }

    private suspend fun resetExpiredGoal(goal: Goal): Goal {
        val resetGoal = GoalResetManager.resetIfExpired(goal)
        if (resetGoal != goal) {
            goalRef(resetGoal.userId, resetGoal.goalId).setValue(resetGoal).await()
        }
        return resetGoal
    }

    private suspend fun awardGoalCompletion(goal: Goal) {
        disciplineRepository?.awardPoints(
            points = goal.goalType.completionReward(),
            eventType = DisciplineEventType.GOAL_COMPLETED,
            description = "Goal completed: ${goal.title} (${goal.goalId})"
        )
    }

    private suspend fun fetchGoal(uid: String, goalId: String): Goal {
        if (goalId.isBlank()) throw GoalException("Goal not found.")
        return goalRef(uid, goalId).get().await().getValue(Goal::class.java)
            ?: throw GoalException("Goal not found.")
    }

    private suspend fun fetchGoals(uid: String): List<Goal> {
        return goalsRef.child(uid).get().await().toGoalList()
    }

    private fun DataSnapshot.toGoalList(): List<Goal> {
        return children.mapNotNull { it.getValue(Goal::class.java) }
            .sortedByDescending { it.createdAt }
    }

    private fun goalRef(uid: String, goalId: String): DatabaseReference {
        return goalsRef.child(uid).child(goalId)
    }

    private fun GoalType.completionReward(): Int {
        return when (this) {
            GoalType.DAILY -> DAILY_GOAL_REWARD
            GoalType.WEEKLY -> WEEKLY_GOAL_REWARD
            GoalType.MONTHLY -> MONTHLY_GOAL_REWARD
        }
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw GoalException("User not authenticated.")
    }

    private suspend fun <T> runCatchingGoal(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(GoalException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is GoalException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load goals."
        return exception.message ?: "Goal request failed. Please try again."
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load goals."
        }
    }

    private class GoalException(message: String) : Exception(message)

    private companion object {
        const val GOALS_PATH = "goals"
        const val DAILY_GOAL_REWARD = 25
        const val WEEKLY_GOAL_REWARD = 75
        const val MONTHLY_GOAL_REWARD = 250
    }
}
