package com.teamdobermans.dopamine_lock.worker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.teamdobermans.dopamine_lock.model.FocusSession
import com.teamdobermans.dopamine_lock.model.Goal
import com.teamdobermans.dopamine_lock.model.Mission
import com.teamdobermans.dopamine_lock.model.MissionStatus
import com.teamdobermans.dopamine_lock.model.User
import kotlinx.coroutines.tasks.await
import java.util.Calendar

object NotificationWorkerDataSource {
    suspend fun hasCompletedSessionToday(): Boolean {
        val uid = currentUid() ?: return false
        return database().reference.child("focusSessions").child(uid).get().await()
            .children
            .mapNotNull { it.getValue(FocusSession::class.java) }
            .any { it.completed && isToday(it.endedAt.takeIf { endedAt -> endedAt > 0L } ?: it.startedAt) }
    }

    suspend fun hasCompletedMissionToday(): Boolean {
        val uid = currentUid() ?: return false
        return database().reference.child("missions").child(uid).get().await()
            .children
            .mapNotNull { it.getValue(Mission::class.java) }
            .any { it.status == MissionStatus.COMPLETED && isToday(it.completedAt) }
    }

    suspend fun hasActiveStreak(): Boolean {
        val uid = currentUid() ?: return false
        val user = database().reference.child("users").child(uid).get().await().getValue(User::class.java)
        return (user?.currentStreak ?: 0) > 0
    }

    suspend fun hasIncompleteActiveGoal(): Boolean {
        val uid = currentUid() ?: return false
        return database().reference.child("goals").child(uid).get().await()
            .children
            .mapNotNull { it.getValue(Goal::class.java) }
            .any { !it.completed && it.currentProgress < it.targetValue }
    }

    suspend fun hasCreatedMissionNotStarted(missionId: String?): Boolean {
        val uid = currentUid() ?: return false
        val missions = database().reference.child("missions").child(uid).get().await()
            .children
            .mapNotNull { it.getValue(Mission::class.java) }

        return missions.any { mission ->
            mission.status == MissionStatus.CREATED &&
                mission.startedAt == 0L &&
                (missionId.isNullOrBlank() || mission.missionId == missionId)
        }
    }

    suspend fun currentStreak(): Int {
        val uid = currentUid() ?: return 0
        return database().reference.child("users").child(uid).get().await()
            .getValue(User::class.java)
            ?.currentStreak ?: 0
    }

    private fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    private fun database(): FirebaseDatabase = FirebaseDatabase.getInstance()

    private fun isToday(timestamp: Long): Boolean {
        if (timestamp <= 0L) return false
        val first = Calendar.getInstance().apply { timeInMillis = timestamp }
        val second = Calendar.getInstance()
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }
}
