package com.teamdobermans.dopamine_lock.data.repositoryImpl

import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.teamdobermans.dopamine_lock.data.repository.AuthRepository
import com.teamdobermans.dopamine_lock.domain.model.User
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    database: com.google.firebase.database.FirebaseDatabase
) : AuthRepository {
    private val usersRef: DatabaseReference = database.reference.child(USERS_PATH)

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User> = runCatchingAuth {
        validateName(name)
        validateEmail(email)
        validatePassword(password)

        val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = authResult.user ?: throw AuthException("Unable to create account. Try again.")
        val now = System.currentTimeMillis()
        val user = User(
            uid = firebaseUser.uid,
            name = name.trim(),
            email = email.trim(),
            createdAt = now,
            updatedAt = now
        )

        usersRef.child(firebaseUser.uid).setValue(user).await()
        user
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> = runCatchingAuth {
        validateEmail(email)
        if (password.isBlank()) throw AuthException("Password is required.")

        val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val uid = authResult.user?.uid ?: throw AuthException("Unable to sign in. Try again.")
        fetchUserProfile(uid)
    }

    override suspend fun logout(): Result<Unit> = runCatchingAuth {
        auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatchingAuth {
        validateEmail(email)
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatchingAuth {
        if (idToken.isBlank()) throw AuthException("Google Sign-In was cancelled.")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw AuthException("Unable to sign in with Google.")
        val existingUser = fetchUserProfileOrNull(firebaseUser.uid)

        existingUser ?: createGoogleUserProfile(firebaseUser)
    }

    override fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    override suspend fun getCurrentUserProfile(): Result<User> = runCatchingAuth {
        val uid = auth.currentUser?.uid ?: throw AuthException("You are not signed in.")
        fetchUserProfile(uid)
    }

    private suspend fun createGoogleUserProfile(firebaseUser: FirebaseUser): User {
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
        usersRef.child(firebaseUser.uid).setValue(user).await()
        return user
    }

    private suspend fun fetchUserProfile(uid: String): User {
        return fetchUserProfileOrNull(uid) ?: throw AuthException("User profile was not found.")
    }

    private suspend fun fetchUserProfileOrNull(uid: String): User? {
        return usersRef.child(uid).get().await().getValue(User::class.java)
    }

    private fun validateName(name: String) {
        if (name.trim().isEmpty()) throw AuthException("Name is required.")
    }

    private fun validateEmail(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) throw AuthException("Email is required.")
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            throw AuthException("Enter a valid email address.")
        }
    }

    private fun validatePassword(password: String) {
        if (password.isBlank()) throw AuthException("Password is required.")
        if (password.length < 6) throw AuthException("Password must be at least 6 characters.")
    }

    private suspend fun <T> runCatchingAuth(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (exception: Exception) {
            Result.failure(AuthException(mapAuthError(exception)))
        }
    }

    private fun mapAuthError(exception: Exception): String {
        if (exception is AuthException) return exception.message.orEmpty()
        if (exception is FirebaseNetworkException) return "Network error. Check your connection and try again."

        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthInvalidUserException -> "No account found for this email."
            is FirebaseAuthInvalidCredentialsException -> "Email or password is incorrect."
            is FirebaseAuthException -> when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
                "ERROR_WRONG_PASSWORD" -> "Email or password is incorrect."
                "ERROR_USER_NOT_FOUND" -> "No account found for this email."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection and try again."
                else -> "Authentication failed. Please try again."
            }
            else -> "Something went wrong. Please try again."
        }
    }

    private class AuthException(message: String) : Exception(message)

    private companion object {
        const val USERS_PATH = "users"
    }
}
