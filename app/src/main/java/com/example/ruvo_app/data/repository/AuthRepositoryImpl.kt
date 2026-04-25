package com.example.ruvo_app.data.repository

import com.example.ruvo_app.data.mapper.AuthErrorMapper
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.AuthRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AuthErrorMapper.map(e))
        }
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al generar identidad")
            
            // Sync with Firestore - Forzamos el UID y el rol seguro
            val newUser = user.copy(id = uid, role = com.example.ruvo_app.domain.model.UserRole.USER)
            val firestoreResult = userRepository.saveUserProfile(newUser)
            
            if (firestoreResult.isSuccess) {
                Result.success(Unit)
            } else {
                // Si falla Firestore, podrías opcionalmente borrar el usuario de Auth aquí
                Result.failure(firestoreResult.exceptionOrNull() ?: Exception("Error al crear perfil"))
            }
        } catch (e: Exception) {
            Result.failure(AuthErrorMapper.map(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AuthErrorMapper.map(e))
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    fullName = it.displayName ?: "",
                    email = it.email ?: "",
                    phone = "",
                    username = ""
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