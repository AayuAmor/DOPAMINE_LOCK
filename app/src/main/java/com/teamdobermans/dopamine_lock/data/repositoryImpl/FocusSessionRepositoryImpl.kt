package com.teamdobermans.dopamine_lock.data.repositoryImpl

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.data.repository.FocusSessionRepository
import com.teamdobermans.dopamine_lock.data.repository.UserRepository
import com.teamdobermans.dopamine_lock.domain.model.FocusSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FocusSessionRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val userRepository: UserRepository
) : FocusSessionRepository {
    private val sessionsRef: DatabaseReference = database.reference.child(SESSIONS_PATH)

    override suspend fun startSession(
        missionName: String,
        missionGoal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>
    ): Result<FocusSession> = runCatchingSession {
        val uid = currentUid()
        val now = System.currentTimeMillis()
        val sessionId = sessionsRef.child(uid).push().key ?: UUID.randomUUID().toString()
        val session = FocusSession(
            sessionId = sessionId,
            userId = uid,
            missionName = missionName.ifBlank { "Focus Mission" },
            missionGoal = missionGoal,
            missionType = missionType.ifBlank { "Deep Work" },
            durationMinutes = durationMinutes,
            blockedApps = blockedApps,
            startedAt = now,
            createdAt = now,
            updatedAt = now
        )
        sessionRef(uid, sessionId).setValue(session).await()
        session
    }

    override suspend fun completeSession(
        sessionId: String,
        elapsedSeconds: Long,
        applyDisciplineScore: Boolean
    ): Result<FocusSession> = runCatchingSession {
        val uid = currentUid()
        val existing = fetchSession(uid, sessionId)
        val now = System.currentTimeMillis()
        val xp = 20 + ((elapsedSeconds / 1800L).toInt() * 5)
        val updated = existing.copy(
            completed = true,
            abandoned = false,
            endedAt = now,
            elapsedSeconds = elapsedSeconds,
            disciplineXp = xp,
            updatedAt = now
        )
        sessionRef(uid, sessionId).setValue(updated).await()
        updateUserStats(elapsedSeconds, xp, applyDisciplineScore)
        updated
    }

    override suspend fun abandonSession(
        sessionId: String,
        elapsedSeconds: Long,
        applyDisciplineScore: Boolean
    ): Result<FocusSession> = runCatchingSession {
        val uid = currentUid()
        val existing = fetchSession(uid, sessionId)
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            completed = false,
            abandoned = true,
            endedAt = now,
            elapsedSeconds = elapsedSeconds,
            disciplineXp = -15,
            updatedAt = now
        )
        sessionRef(uid, sessionId).setValue(updated).await()
        updateUserStats(elapsedSeconds = 0L, xp = -15, applyDisciplineScore = applyDisciplineScore)
        updated
    }

    override suspend fun getSessionById(sessionId: String): Result<FocusSession> = runCatchingSession {
        fetchSession(currentUid(), sessionId)
    }

    override suspend fun getCurrentUserSessions(): Result<List<FocusSession>> = runCatchingSession {
        fetchSessions(currentUid())
    }

    override suspend fun getSessionsByDateRange(startTime: Long, endTime: Long): Result<List<FocusSession>> = runCatchingSession {
        fetchSessions(currentUid())
            .filter { it.startedAt in startTime..endTime }
            .sortedByDescending { it.startedAt }
    }

    override fun observeCurrentUserSessions(): Flow<Result<List<FocusSession>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(SessionException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = sessionsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(snapshot.toSessionList()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(SessionException(mapDatabaseError(error))))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeActiveSession(): Flow<Result<FocusSession?>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(SessionException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = sessionsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeSession = snapshot.toSessionList()
                    .filter { !it.completed && !it.abandoned && it.endedAt == 0L }
                    .maxByOrNull { it.startedAt }
                trySend(Result.success(activeSession))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(SessionException(mapDatabaseError(error))))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> = runCatchingSession {
        sessionRef(currentUid(), sessionId).removeValue().await()
    }

    private suspend fun updateUserStats(
        elapsedSeconds: Long,
        xp: Int,
        applyDisciplineScore: Boolean
    ) {
        userRepository.getCurrentUserProfile().onSuccess { user ->
            if (elapsedSeconds > 0L) {
                userRepository.updateTotalFocusHours(
                    uid = user.uid,
                    totalFocusHours = user.totalFocusHours + elapsedSeconds / 3600.0
                )
            }
            if (applyDisciplineScore) {
                userRepository.updateDisciplineScore(
                    uid = user.uid,
                    score = (user.disciplineScore + xp).coerceAtLeast(0)
                )
            }
        }
    }

    private suspend fun fetchSession(uid: String, sessionId: String): FocusSession {
        return sessionRef(uid, sessionId).get().await().getValue(FocusSession::class.java)
            ?: throw SessionException("Session not found.")
    }

    private suspend fun fetchSessions(uid: String): List<FocusSession> {
        return sessionsRef.child(uid).get().await().toSessionList()
    }

    private fun DataSnapshot.toSessionList(): List<FocusSession> {
        return children.mapNotNull { it.getValue(FocusSession::class.java) }
            .sortedByDescending { it.startedAt }
    }

    private fun sessionRef(uid: String, sessionId: String): DatabaseReference {
        return sessionsRef.child(uid).child(sessionId)
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw SessionException("User not authenticated.")
    }

    private suspend fun <T> runCatchingSession(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(SessionException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is SessionException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load sessions."
        return "Session request failed. Please try again."
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load sessions."
        }
    }

    private class SessionException(message: String) : Exception(message)

    private companion object {
        const val SESSIONS_PATH = "focusSessions"
    }
}
