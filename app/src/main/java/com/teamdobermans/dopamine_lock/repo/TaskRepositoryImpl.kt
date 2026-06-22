package com.teamdobermans.dopamine_lock.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.model.Task
import com.teamdobermans.dopamine_lock.model.TaskPriority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TaskRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase
) : TaskRepository {

    private val tasksRef: DatabaseReference = database.reference.child(TASKS_PATH)

    override fun observeTasks(): Flow<List<Task>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = snapshot.children
                    .mapNotNull { it.getValue(Task::class.java) }
                    .sortedWith(compareBy({ it.completed }, { -it.createdAt }))
                trySend(tasks)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        tasksRef.child(uid).addValueEventListener(listener)
        awaitClose { tasksRef.child(uid).removeEventListener(listener) }
    }

    override suspend fun createTask(
        title: String,
        description: String,
        category: String,
        dueDate: String,
        priority: TaskPriority
    ): Result<Task> = runCatching {
        val uid = requireUid()
        if (title.trim().isEmpty()) error("Task title is required.")
        val taskId = tasksRef.child(uid).push().key ?: UUID.randomUUID().toString()
        val task = Task(
            taskId = taskId,
            userId = uid,
            title = title.trim(),
            description = description.trim(),
            category = category.trim(),
            dueDate = dueDate.trim(),
            priority = priority,
            completed = false,
            createdAt = System.currentTimeMillis()
        )
        tasksRef.child(uid).child(taskId).setValue(task).await()
        task
    }

    override suspend fun toggleTask(taskId: String, completed: Boolean): Result<Unit> = runCatching {
        val uid = requireUid()
        val updates = mapOf<String, Any>(
            "completed" to completed,
            "completedAt" to if (completed) System.currentTimeMillis() else 0L
        )
        tasksRef.child(uid).child(taskId).updateChildren(updates).await()
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> = runCatching {
        val uid = requireUid()
        tasksRef.child(uid).child(taskId).removeValue().await()
    }

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("User is not signed in.")

    private companion object {
        const val TASKS_PATH = "tasks"
    }
}
