package com.teamdobermans.dopamine_lock.repo

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.DisciplineEventType
import com.teamdobermans.dopamine_lock.model.DisciplineRank
import com.teamdobermans.dopamine_lock.util.DisciplineRankCalculator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DisciplineRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val userRepository: UserRepository
) : DisciplineRepository {
    private val eventsRef: DatabaseReference = database.reference.child(DISCIPLINE_EVENTS_PATH)

    override suspend fun addDisciplineEvent(
        eventType: DisciplineEventType,
        points: Int,
        description: String,
        relatedMissionId: String?,
        relatedSessionId: String?
    ): Result<DisciplineEvent> = runCatchingDiscipline {
        val uid = currentUid()
        val duplicate = findDuplicateEvent(uid, eventType, description, relatedMissionId, relatedSessionId)
        if (duplicate != null) return@runCatchingDiscipline duplicate

        val eventId = eventsRef.child(uid).push().key ?: UUID.randomUUID().toString()
        val event = DisciplineEvent(
            eventId = eventId,
            userId = uid,
            eventType = eventType.name,
            points = points,
            description = description,
            relatedMissionId = relatedMissionId,
            relatedSessionId = relatedSessionId,
            createdAt = System.currentTimeMillis()
        )

        eventsRef.child(uid).child(eventId).setValue(event).await()
        event
    }

    override suspend fun awardPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String?,
        relatedSessionId: String?
    ): Result<Int> = changeScore(
        points = points.coerceAtLeast(0),
        eventType = eventType,
        description = description,
        relatedMissionId = relatedMissionId,
        relatedSessionId = relatedSessionId
    )

    override suspend fun deductPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String?,
        relatedSessionId: String?
    ): Result<Int> = changeScore(
        points = -points.coerceAtLeast(0),
        eventType = eventType,
        description = description,
        relatedMissionId = relatedMissionId,
        relatedSessionId = relatedSessionId
    )

    override suspend fun getCurrentScore(): Result<Int> = runCatchingDiscipline {
        userRepository.getCurrentUserProfile().getOrThrow().disciplineScore
    }

    override suspend fun getRank(): Result<DisciplineRank> = runCatchingDiscipline {
        DisciplineRankCalculator.calculateRank(getCurrentScore().getOrThrow())
    }

    override suspend fun getEventHistory(): Result<List<DisciplineEvent>> = runCatchingDiscipline {
        fetchEvents(currentUid())
    }

    override fun observeScore(): Flow<Result<Int>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(DisciplineException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val job = launch {
            userRepository.observeUserProfile(uid).collect { result ->
                result
                    .onSuccess { user -> trySend(Result.success(user.disciplineScore)) }
                    .onFailure { exception -> trySend(Result.failure(exception)) }
            }
        }
        awaitClose { job.cancel() }
    }

    override fun observeRank(): Flow<Result<DisciplineRank>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(DisciplineException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val job = launch {
            userRepository.observeUserProfile(uid).collect { result ->
                result
                    .onSuccess { user ->
                        trySend(Result.success(DisciplineRankCalculator.calculateRank(user.disciplineScore)))
                    }
                    .onFailure { exception -> trySend(Result.failure(exception)) }
            }
        }
        awaitClose { job.cancel() }
    }

    override fun observeEventHistory(): Flow<Result<List<DisciplineEvent>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(DisciplineException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = eventsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(snapshot.toDisciplineEventList()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(DisciplineException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun changeScore(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String?,
        relatedSessionId: String?
    ): Result<Int> = runCatchingDiscipline {
        val uid = currentUid()
        val duplicate = findDuplicateEvent(uid, eventType, description, relatedMissionId, relatedSessionId)
        if (duplicate != null) return@runCatchingDiscipline userRepository.getUserProfile(uid).getOrThrow().disciplineScore

        val user = userRepository.getUserProfile(uid).getOrThrow()
        val updatedScore = (user.disciplineScore + points).coerceAtLeast(0)
        val event = addDisciplineEvent(
            eventType = eventType,
            points = points,
            description = description,
            relatedMissionId = relatedMissionId,
            relatedSessionId = relatedSessionId
        ).getOrThrow()

        if (event.points == points) {
            userRepository.updateDisciplineScore(uid = uid, score = updatedScore).getOrThrow()
        }

        updatedScore
    }

    private suspend fun findDuplicateEvent(
        uid: String,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String?,
        relatedSessionId: String?
    ): DisciplineEvent? {
        return fetchEvents(uid).firstOrNull { event ->
            event.eventType == eventType.name &&
                event.description == description &&
                event.relatedMissionId == relatedMissionId &&
                event.relatedSessionId == relatedSessionId
        }
    }

    private suspend fun fetchEvents(uid: String): List<DisciplineEvent> {
        return eventsRef.child(uid).get().await().toDisciplineEventList()
    }

    private fun DataSnapshot.toDisciplineEventList(): List<DisciplineEvent> {
        return children.mapNotNull { it.getValue(DisciplineEvent::class.java) }
            .sortedByDescending { it.createdAt }
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw DisciplineException("User not authenticated.")
    }

    private suspend fun <T> runCatchingDiscipline(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(DisciplineException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is DisciplineException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load discipline events."
        return exception.message ?: "Discipline request failed. Please try again."
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load discipline events."
        }
    }

    private class DisciplineException(message: String) : Exception(message)

    private companion object {
        const val DISCIPLINE_EVENTS_PATH = "disciplineEvents"
    }
}
