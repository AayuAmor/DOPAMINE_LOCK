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
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.model.MissionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MissionRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase,
    private val userRepository: UserRepository,
    private val streakRepository: StreakRepository? = null,
    private val disciplineRepository: DisciplineRepository? = null
) : MissionRepository {
    private val missionsRef: DatabaseReference = database.reference.child(MISSIONS_PATH)

    override suspend fun createMission(
        title: String,
        goal: String,
        missionType: String,
        durationMinutes: Int,
        blockedApps: List<String>
    ): Result<Mission> = runCatchingMission {
        val uid = currentUid()
        val now = System.currentTimeMillis()
        val missionId = missionsRef.child(uid).push().key ?: UUID.randomUUID().toString()
        val mission = Mission(
            missionId = missionId,
            userId = uid,
            title = title.ifBlank { "Focus Mission" },
            goal = goal,
            missionType = missionType.ifBlank { "Deep Work" },
            durationMinutes = durationMinutes,
            blockedApps = blockedApps,
            status = MissionStatus.CREATED,
            createdAt = now
        )

        missionRef(uid, missionId).setValue(mission).await()
        mission
    }

    override suspend fun startMission(missionId: String): Result<Mission> = runCatchingMission {
        val uid = currentUid()
        val existing = fetchMission(uid, missionId)
        ensureTransition(existing.status, MissionStatus.ACTIVE)

        val updated = existing.copy(
            status = MissionStatus.ACTIVE,
            startedAt = System.currentTimeMillis()
        )
        missionRef(uid, missionId).setValue(updated).await()
        updated
    }

    override suspend fun completeMission(missionId: String): Result<Mission> = runCatchingMission {
        finishMission(
            missionId = missionId,
            status = MissionStatus.COMPLETED,
            reward = COMPLETION_REWARD,
            penalty = 0
        )
    }

    override suspend fun abandonMission(missionId: String): Result<Mission> = runCatchingMission {
        finishMission(
            missionId = missionId,
            status = MissionStatus.ABANDONED,
            reward = 0,
            penalty = ABANDON_PENALTY
        )
    }

    override suspend fun failMission(missionId: String): Result<Mission> = runCatchingMission {
        finishMission(
            missionId = missionId,
            status = MissionStatus.FAILED,
            reward = 0,
            penalty = FAILURE_PENALTY
        )
    }

    override suspend fun getMission(missionId: String): Result<Mission> = runCatchingMission {
        fetchMission(currentUid(), missionId)
    }

    override suspend fun getCurrentUserMissions(): Result<List<Mission>> = runCatchingMission {
        fetchMissions(currentUid())
    }

    override fun observeActiveMission(): Flow<Result<Mission?>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(MissionException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = missionsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeMission = snapshot.toMissionList()
                    .filter { it.status == MissionStatus.ACTIVE }
                    .maxByOrNull { it.startedAt }
                trySend(Result.success(activeMission))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(MissionException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun observeUserMissions(): Flow<Result<List<Mission>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.failure(MissionException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val ref = missionsRef.child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(Result.success(snapshot.toMissionList()))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(MissionException(mapDatabaseError(error))))
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    private suspend fun finishMission(
        missionId: String,
        status: MissionStatus,
        reward: Int,
        penalty: Int
    ): Mission {
        val uid = currentUid()
        val existing = fetchMission(uid, missionId)
        ensureTransition(existing.status, status)

        val updated = existing.copy(
            status = status,
            completedAt = System.currentTimeMillis(),
            disciplineReward = reward,
            disciplinePenalty = penalty
        )
        missionRef(uid, missionId).setValue(updated).await()

        applyDisciplineEvent(updated)

        streakRepository?.evaluateToday()
        return updated
    }

    private suspend fun applyDisciplineEvent(mission: Mission) {
        when (mission.status) {
            MissionStatus.COMPLETED -> disciplineRepository?.awardPoints(
                points = COMPLETION_REWARD,
                eventType = DisciplineEventType.MISSION_COMPLETED,
                description = "Mission completed: ${mission.title}",
                relatedMissionId = mission.missionId
            )
            MissionStatus.ABANDONED -> disciplineRepository?.deductPoints(
                points = ABANDON_PENALTY,
                eventType = DisciplineEventType.MISSION_ABANDONED,
                description = "Mission abandoned: ${mission.title}",
                relatedMissionId = mission.missionId
            )
            MissionStatus.FAILED -> disciplineRepository?.deductPoints(
                points = FAILURE_PENALTY,
                eventType = DisciplineEventType.MISSION_FAILED,
                description = "Mission failed: ${mission.title}",
                relatedMissionId = mission.missionId
            )
            MissionStatus.CREATED,
            MissionStatus.ACTIVE -> Unit
        }
    }

    private fun ensureTransition(from: MissionStatus, to: MissionStatus) {
        val isValid = when (from) {
            MissionStatus.CREATED -> to == MissionStatus.ACTIVE
            MissionStatus.ACTIVE -> to in terminalStatuses
            MissionStatus.COMPLETED -> false
            MissionStatus.ABANDONED -> false
            MissionStatus.FAILED -> false
        }

        if (!isValid) {
            throw MissionException(transitionError(from, to))
        }
    }

    private fun transitionError(from: MissionStatus, to: MissionStatus): String {
        return when (from) {
            MissionStatus.COMPLETED -> "Mission already completed."
            MissionStatus.ABANDONED -> "Mission already abandoned."
            MissionStatus.FAILED -> "Mission already failed."
            else -> "Invalid mission status transition: ${from.name} to ${to.name}."
        }
    }

    private suspend fun fetchMission(uid: String, missionId: String): Mission {
        if (missionId.isBlank()) throw MissionException("Mission not found.")
        return missionRef(uid, missionId).get().await().getValue(Mission::class.java)
            ?: throw MissionException("Mission not found.")
    }

    private suspend fun fetchMissions(uid: String): List<Mission> {
        return missionsRef.child(uid).get().await().toMissionList()
    }

    private fun DataSnapshot.toMissionList(): List<Mission> {
        return children.mapNotNull { it.getValue(Mission::class.java) }
            .sortedByDescending { it.createdAt }
    }

    private fun missionRef(uid: String, missionId: String): DatabaseReference {
        return missionsRef.child(uid).child(missionId)
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw MissionException("User not authenticated.")
    }

    private suspend fun <T> runCatchingMission(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(MissionException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is MissionException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."
        if (exception is DatabaseException) return "Failed to load missions."
        return "Mission request failed. Please try again."
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load missions."
        }
    }

    private class MissionException(message: String) : Exception(message)

    private companion object {
        const val MISSIONS_PATH = "missions"
        const val COMPLETION_REWARD = 20
        const val ABANDON_PENALTY = 15
        const val FAILURE_PENALTY = 25

        val terminalStatuses = setOf(
            MissionStatus.COMPLETED,
            MissionStatus.ABANDONED,
            MissionStatus.FAILED
        )
    }
}
