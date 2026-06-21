package com.teamdobermans.dopamine_lock.data.repository

import com.google.firebase.auth.FirebaseUser
import com.teamdobermans.dopamine_lock.domain.model.User

interface AuthRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User>

    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun sendPasswordReset(
        email: String
    ): Result<Unit>

    suspend fun signInWithGoogle(
        idToken: String
    ): Result<User>

    fun getCurrentFirebaseUser(): FirebaseUser?

    suspend fun getCurrentUserProfile(): Result<User>
}
