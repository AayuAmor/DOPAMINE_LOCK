package com.teamdobermans.dopamine_lock.repo

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.repo.StreakRepository
import com.teamdobermans.dopamine_lock.repo.UserRepository
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.model.MissionStatus
import com.teamdobermans.dopamine_lock.model.StreakRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val userRepository: UserRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : StreakRepository {
    private val streakRecordsRef: DatabaseReference = database.reference.child(STREAK_RECORDS_PATH)
    private val missionsRef: DatabaseReference = database.reference.child(MISSIONS_PATH)
    private val sessionsRef: DatabaseReference = database.reference.child(SESSIONS_PATH)

    override suspend fun evaluateToday(): Result<StreakRecord> = runCatchingStreak {
        val uid = currentUid()
        val today = LocalDate.now(zoneId)
        val dateKey = today.toString()
        val missionsCompleted = fetchMissions(uid)
            .count { it.status == MissionStatus.COMPLETED && isSameDate(it.completedAt, today) }
        val completedSessions = fetchSessions(uid)
            .filter { it.completed && isSameDate(it.completedAtOrStartedAt(), today) }
        val focusMinutes = completedSessions.sumOf { (it.elapsedSeconds / SECONDS_PER_MINUTE).toInt() }
        val dailyGoalMinutes = completedSessions
            .sumOf { it.durationMinutes.coerceAtLeast(0) }
            .coerceAtLeast(DEFAULT_DAILY_GOAL_MINUTES)
        val successful = missionsCompleted > 0 && focusMinutes >= dailyGoalMinutes
        val existing = streakRecordsRef.child(uid).child(dateKey).get().await().getValue(StreakRecord::class.java)
        val record = StreakRecord(
            recordId = dateKey,
            userId = uid,
            date = dateKey,
            successful = successful,
            missionsCompleted = missionsCompleted,
            focusMinutes = focusMinutes,
            dailyGoalMinutes = dailyGoalMinutes,
            createdAt = existing?.createdAt?.takeIf { it > 0L } ?: System.currentTimeMillis()
        )

        streakRecordsRef.child(uid).child(dateKey).setValue(record).await()
        updateUserStreaks(uid)
        record
    }

    override suspend fun calculateCurrentStreak(): Result<Int> = runCatchingStreak {
        calculateCurrentStreak(fetchRecords(currentUid()))
    }

    override suspend fun calculateBestStreak(): Result<Int> = runCatchingStreak {
        calculateBestStreak(fetchRecords(currentUid()))
    }

    override suspend fun getTodayRecord(): Result<StreakRecord?> = runCatchingStreak {
        val uid = currentUid()
        streakRecordsRef.child(uid).child(LocalDate.now(zoneId).toString()).get().await()
            .getValue(StreakRecord::class.java)
    }

    override suspend fun getStreakCalendar(): Result<List<StreakRecord>> = runCatchingStreak {
        fetchRecords(currentUid())
    }

    override fun observeCurrentStreak(): Flow<Result<Int>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(StreakException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = streakRecordsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(calculateCurrentStreak(snapshot.toStreakRecordList())))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(StreakException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeStreakRecords(): Flow<Result<List<StreakRecord>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(StreakException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = streakRecordsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(snapshot.toStreakRecordList()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(StreakException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun updateUserStreaks(uid: String) {
        val records = fetchRecords(uid)
        val currentStreak = calculateCurrentStreak(records)
        val calculatedBestStreak = calculateBestStreak(records)
        val existingBestStreak = userRepository.getUserProfile(uid).getOrNull()?.bestStreak ?: 0

        userRepository.updateStreaks(
            uid = uid,
            currentStreak = currentStreak,
            bestStreak = maxOf(existingBestStreak, calculatedBestStreak)
        ).getOrThrow()
    }

    private fun calculateCurrentStreak(records: List<StreakRecord>): Int {
        val recordByDate = records.associateBy { it.date }
        var date = LocalDate.now(zoneId)
        var streak = 0

        while (recordByDate[date.toString()]?.successful == true) {
            streak++
            date = date.minusDays(1)
        }

        return streak
    }

    private fun calculateBestStreak(records: List<StreakRecord>): Int {
        var bestStreak = 0
        var runningStreak = 0
        var previousDate: LocalDate? = null

        records.sortedBy { it.date }.forEach { record ->
            val recordDate = runCatching { LocalDate.parse(record.date) }.getOrNull() ?: return@forEach
            runningStreak = if (record.successful) {
                if (previousDate?.plusDays(1) == recordDate) runningStreak + 1 else 1
            } else {
                0
            }
            bestStreak = maxOf(bestStreak, runningStreak)
            previousDate = recordDate
        }

        return bestStreak
    }

    private suspend fun fetchRecords(uid: String): List<StreakRecord> {
        return streakRecordsRef.child(uid).get().await().toStreakRecordList()
    }

    private suspend fun fetchMissions(uid: String): List<Mission> {
        return missionsRef.child(uid).get().await().children.mapNotNull { it.getValue(Mission::class.java) }
    }

    private suspend fun fetchSessions(uid: String): List<FocusSession> {
        return sessionsRef.child(uid).get().await().children.mapNotNull { it.getValue(FocusSession::class.java) }
    }

    private fun DataSnapshot.toStreakRecordList(): List<StreakRecord> {
        return children.mapNotNull { it.getValue(StreakRecord::class.java) }.sortedByDescending { it.date }
    }

    private fun FocusSession.completedAtOrStartedAt(): Long {
        return endedAt.takeIf { it > 0L } ?: startedAt
    }

    private fun isSameDate(timestamp: Long, date: LocalDate): Boolean {
        if (timestamp <= 0L) return false
        return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate() == date
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw StreakException("User not authenticated.")
    }

    private suspend fun <T> runCatchingStreak(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(StreakException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is StreakException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load streak records."
        return "Streak request failed. Please try again."
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load streak records."
        }
    }

    private class StreakException(message: String) : Exception(message)

    private companion object {
        const val STREAK_RECORDS_PATH = "streakRecords"
        const val MISSIONS_PATH = "missions"
        const val SESSIONS_PATH = "focusSessions"
        const val DEFAULT_DAILY_GOAL_MINUTES = 25
        const val SECONDS_PER_MINUTE = 60L
    }
}
