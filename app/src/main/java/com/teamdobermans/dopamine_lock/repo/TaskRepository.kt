package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.Task
import com.teamdobermans.dopamine_lock.model.TaskPriority
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun createTask(
        title: String,
        description: String,
        category: String,
        dueDate: String,
        priority: TaskPriority
    ): Result<Task>
    suspend fun toggleTask(taskId: String, completed: Boolean): Result<Unit>
    suspend fun deleteTask(taskId: String): Result<Unit>
}
