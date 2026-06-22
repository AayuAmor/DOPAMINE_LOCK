package com.teamdobermans.dopamine_lock.model

data class Task(
    val taskId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val dueDate: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val completed: Boolean = false,
    val createdAt: Long = 0L,
    val completedAt: Long = 0L
)

enum class TaskPriority { HIGH, MEDIUM, LOW }
