package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.Task
import com.teamdobermans.dopamine_lock.model.TaskPriority
import com.teamdobermans.dopamine_lock.repo.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val pendingTasks: List<Task> get() = tasks.filter { !it.completed }
    val completedTasks: List<Task> get() = tasks.filter { it.completed }
}

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun observeTasks() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            taskRepository.observeTasks().collect { tasks ->
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        category: String,
        dueDate: String,
        priority: TaskPriority
    ) {
        if (title.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Task title is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            taskRepository.createTask(title, description, category, dueDate, priority)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Task created.") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Failed to create task.") }
                }
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTask(taskId, completed)
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update task.") }
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete task.") }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clear() {
        observeJob?.cancel()
        _uiState.value = TaskUiState()
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}

class TaskViewModelFactory(private val taskRepository: TaskRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
