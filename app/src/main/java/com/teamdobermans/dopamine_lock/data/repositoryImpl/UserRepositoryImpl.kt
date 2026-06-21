package com.teamdobermans.dopamine_lock.data.repositoryImpl

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamdobermans.dopamine_lock.data.repository.UserRepository
import com.teamdobermans.dopamine_lock.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    database: FirebaseDatabase
) : UserRepository {
    private val usersRef: DatabaseReference = database.reference.child(USERS_PATH)

    override suspend fun createUserProfile(user: User): Result<Unit> = runCatchingUser {
        if (user.uid.isBlank()) throw UserException("User not authenticated.")

        val now = System.currentTimeMillis()
        val profile = user.copy(
            createdAt = user.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )

        usersRef.child(profile.uid).setValue(profile).await()
    }

    override suspend fun getUserProfile(uid: String): Result<User> = runCatchingUser {
        if (uid.isBlank()) throw UserException("User not authenticated.")
        fetchUserProfile(uid)
    }

    override suspend fun getCurrentUserProfile(): Result<User> = runCatchingUser {
        val uid = auth.currentUser?.uid ?: throw UserException("User not authenticated.")
        fetchUserProfile(uid)
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = runCatchingUser {
        if (user.uid.isBlank()) throw UserException("User not authenticated.")

        val existing = fetchUserProfile(user.uid)
        val updatedUser = user.copy(
            uid = existing.uid,
            createdAt = existing.createdAt,
            updatedAt = System.currentTimeMillis()
        )

        usersRef.child(user.uid).setValue(updatedUser).await()
    }

    override suspend fun updateUserName(uid: String, name: String): Result<Unit> = runCatchingUser {
        if (uid.isBlank()) throw UserException("User not authenticated.")
        if (name.trim().isEmpty()) throw UserException("Name is required.")

        usersRef.child(uid).updateChildren(
            mapOf(
                "name" to name.trim(),
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun updateDisciplineScore(uid: String, score: Int): Result<Unit> = runCatchingUser {
        if (uid.isBlank()) throw UserException("User not authenticated.")

        usersRef.child(uid).updateChildren(
            mapOf(
                "disciplineScore" to score,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun updateStreaks(
        uid: String,
        currentStreak: Int,
        bestStreak: Int
    ): Result<Unit> = runCatchingUser {
        if (uid.isBlank()) throw UserException("User not authenticated.")

        usersRef.child(uid).updateChildren(
            mapOf(
                "currentStreak" to currentStreak,
                "bestStreak" to bestStreak,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override suspend fun updateTotalFocusHours(
        uid: String,
        totalFocusHours: Double
    ): Result<Unit> = runCatchingUser {
        if (uid.isBlank()) throw UserException("User not authenticated.")

        usersRef.child(uid).updateChildren(
            mapOf(
                "totalFocusHours" to totalFocusHours,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    override fun observeUserProfile(uid: String): Flow<Result<User>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(Result.failure(UserException("User not authenticated.")))
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(UserException("Profile not found."))
                    }
                )
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(UserException(mapDatabaseError(error))))
            }
        }

        val ref = usersRef.child(uid)
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun ensureUserProfileExists(): Result<User> = runCatchingUser {
        val firebaseUser = auth.currentUser ?: throw UserException("User not authenticated.")
        val existingProfile = fetchUserProfileOrNull(firebaseUser.uid)

        existingProfile ?: run {
            val now = System.currentTimeMillis()
            val user = User(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Focus Warrior",
                email = firebaseUser.email.orEmpty(),
                disciplineScore = 0,
                currentStreak = 0,
                bestStreak = 0,
                totalFocusHours = 0.0,
                createdAt = now,
                updatedAt = now
            )
            usersRef.child(user.uid).setValue(user).await()
            user
        }
    }

    private suspend fun fetchUserProfile(uid: String): User {
        return fetchUserProfileOrNull(uid) ?: throw UserException("Profile not found.")
    }

    private suspend fun fetchUserProfileOrNull(uid: String): User? {
        return usersRef.child(uid).get().await().getValue(User::class.java)
    }

    private suspend fun <T> runCatchingUser(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(UserException(mapException(exception)))
        }
    }

    private fun mapException(exception: Exception): String {
        if (exception is UserException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."

        return when (exception) {
            is DatabaseException -> "Failed to load profile."
            else -> "Database error. Please try again."
        }
    }

    private fun mapDatabaseError(error: DatabaseError): String {
        return when (error.code) {
            DatabaseError.PERMISSION_DENIED -> "Permission denied."
            DatabaseError.NETWORK_ERROR -> "Network error. Check your connection and try again."
            else -> "Failed to load profile."
        }
    }

    private class UserException(message: String) : Exception(message)

    private companion object {
        const val USERS_PATH = "users"
    }
}
