package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<Result<User>> = callbackFlow {
        val subscription = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(UserDto::class.java)?.toDomain(uid)
                    if (user != null) {
                        trySend(Result.success(user))
                    } else {
                        trySend(Result.failure(Exception("Error al mapear usuario")))
                    }
                } else {
                    trySend(Result.failure(Exception("Usuario no encontrado")))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id)
                .set(UserDto.fromDomain(user))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// DTO for Firestore
data class UserDto(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "USER",
    val points: Int = 0,
    val level: String = "PRINCIPIANTE",
    val badges: List<String> = emptyList(),
    val activePosts: Int = 0,
    val finishedPosts: Int = 0,
    val pendingVerification: Int = 0
) {
    fun toDomain(id: String) = User(
        id = id,
        fullName = fullName,
        username = username,
        email = email,
        phone = phone,
        role = UserRole.valueOf(role),
        reputation = Reputation(
            points = points,
            level = UserLevel.valueOf(level),
            badges = badges
        ),
        stats = UserStats(
            activePosts = activePosts,
            finishedPosts = finishedPosts,
            pendingVerification = pendingVerification
        )
    )

    companion object {
        fun fromDomain(user: User) = UserDto(
            fullName = user.fullName,
            username = user.username,
            email = user.email,
            phone = user.phone,
            role = user.role.name,
            points = user.reputation.points,
            level = user.reputation.level.name,
            badges = user.reputation.badges,
            activePosts = user.stats.activePosts,
            finishedPosts = user.stats.finishedPosts,
            pendingVerification = user.stats.pendingVerification
        )
    }
}