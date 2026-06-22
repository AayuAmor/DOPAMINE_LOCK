package com.teamdobermans.dopamine_lock.viewModel

import com.teamdobermans.dopamine_lock.model.DisciplineEvent
import com.teamdobermans.dopamine_lock.model.DisciplineRank

data class DisciplineUiState(
    val isLoading: Boolean = false,
    val score: Int = 0,
    val rank: DisciplineRank = DisciplineRank.D,
    val events: List<DisciplineEvent> = emptyList(),
    val recentEvents: List<DisciplineEvent> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)
