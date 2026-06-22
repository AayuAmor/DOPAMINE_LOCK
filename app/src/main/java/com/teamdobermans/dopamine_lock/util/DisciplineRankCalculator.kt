package com.teamdobermans.dopamine_lock.util

import com.teamdobermans.dopamine_lock.model.DisciplineRank

object DisciplineRankCalculator {
    fun calculateRank(score: Int): DisciplineRank {
        return when {
            score >= 2500 -> DisciplineRank.SS
            score >= 1000 -> DisciplineRank.S
            score >= 500 -> DisciplineRank.A
            score >= 250 -> DisciplineRank.B
            score >= 100 -> DisciplineRank.C
            else -> DisciplineRank.D
        }
    }

    fun nextRank(score: Int): DisciplineRank? {
        return when {
            score < 100 -> DisciplineRank.C
            score < 250 -> DisciplineRank.B
            score < 500 -> DisciplineRank.A
            score < 1000 -> DisciplineRank.S
            score < 2500 -> DisciplineRank.SS
            else -> null
        }
    }

    fun currentRankFloor(score: Int): Int {
        return when {
            score >= 2500 -> 2500
            score >= 1000 -> 1000
            score >= 500 -> 500
            score >= 250 -> 250
            score >= 100 -> 100
            else -> 0
        }
    }

    fun nextRankTarget(score: Int): Int {
        return when {
            score < 100 -> 100
            score < 250 -> 250
            score < 500 -> 500
            score < 1000 -> 1000
            score < 2500 -> 2500
            else -> 2500
        }
    }
}
