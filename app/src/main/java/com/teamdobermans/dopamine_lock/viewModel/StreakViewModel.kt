package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.StreakRecord
import com.teamdobermans.dopamine_lock.repo.StreakRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StreakUiState(
    val isLoading: Boolean = false,
    val streakRecords: List<StreakRecord> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val errorMessage: String? = null
)

class StreakViewModel(
    private val repository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakUiState())
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    private var recordsJob: Job? = null

    fun observeStreakRecords() {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeStreakRecords().collect { result ->
                result
                    .onSuccess { records ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                streakRecords = records,
                                currentStreak = calcCurrentStreak(records),
                                bestStreak = calcBestStreak(records)
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
            }
        }
    }

    fun clear() {
        recordsJob?.cancel()
        recordsJob = null
        _uiState.value = StreakUiState()
    }

    companion object {
        fun calcCurrentStreak(records: List<StreakRecord>): Int {
            val byDate = records.associateBy { it.date }
            var date = LocalDate.now()
            var streak = 0
            while (byDate[date.toString()]?.successful == true) {
                streak++
                date = date.minusDays(1)
            }
            return streak
        }

        fun calcBestStreak(records: List<StreakRecord>): Int {
            var best = 0
            var running = 0
            var prev: LocalDate? = null
            records.sortedBy { it.date }.forEach { record ->
                val d = runCatching { LocalDate.parse(record.date) }.getOrNull() ?: return@forEach
                running = if (record.successful) {
                    if (prev?.plusDays(1) == d) running + 1 else 1
                } else 0
                best = maxOf(best, running)
                prev = d
            }
            return best
        }
    }
}

class StreakViewModelFactory(
    private val repository: StreakRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StreakViewModel::class.java)) {
            return StreakViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
