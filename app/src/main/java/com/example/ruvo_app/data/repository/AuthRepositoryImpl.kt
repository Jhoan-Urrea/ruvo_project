package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.AuthRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Error al obtener UID"))
            
            // Sync with Firestore
            val newUser = user.copy(id = uid)
            userRepository.saveUserProfile(newUser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            // Here we only emit if there's a basic user to trigger Firestore fetch later
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    fullName = it.displayName ?: "",
                    username = "",
                    email = it.email ?: ""
                )
            }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}