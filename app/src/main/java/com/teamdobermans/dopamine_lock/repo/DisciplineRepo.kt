package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.DisciplineEventType
import com.teamdobermans.dopamine_lock.model.DisciplineRank
import kotlinx.coroutines.flow.Flow

interface DisciplineRepository {
    suspend fun addDisciplineEvent(
        eventType: DisciplineEventType,
        points: Int,
        description: String,
        relatedMissionId: String? = null,
        relatedSessionId: String? = null
    ): Result<DisciplineEvent>

    suspend fun awardPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String? = null,
        relatedSessionId: String? = null
    ): Result<Int>

    suspend fun deductPoints(
        points: Int,
        eventType: DisciplineEventType,
        description: String,
        relatedMissionId: String? = null,
        relatedSessionId: String? = null
    ): Result<Int>

    suspend fun getCurrentScore(): Result<Int>

    suspend fun getRank(): Result<DisciplineRank>

    suspend fun getEventHistory(): Result<List<DisciplineEvent>>

    fun observeScore(): Flow<Result<Int>>

    fun observeRank(): Flow<Result<DisciplineRank>>

    fun observeEventHistory(): Flow<Result<List<DisciplineEvent>>>
}
