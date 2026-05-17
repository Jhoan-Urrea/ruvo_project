package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
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

    override suspend fun addUserPoints(uid: String, points: Int): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(uid)
            
            // Usamos un transaction para asegurar que el nivel se actualice correctamente basado en los nuevos puntos
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentPoints = snapshot.getLong("points") ?: 0L
                val newPoints = currentPoints + points
                val newLevel = UserLevel.fromPoints(newPoints.toInt())
                
                transaction.update(userRef, "points", newPoints)
                transaction.update(userRef, "level", newLevel.name)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val subscription = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }
}

// DTO for Firestore
data class UserDto(
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePictureUrl: String? = null,
    val role: String = "USER",
    val points: Int = 0,
    val level: String = "PRINCIPIANTE",
    val badges: List<String> = emptyList(),
    val activePosts: Int = 0,
    val finishedPosts: Int = 0,
    val pendingVerification: Int = 0,
    val totalReviews: Int = 0
) {
    fun toDomain(id: String) = User(
        id = id,
        fullName = fullName,
        username = username,
        email = email,
        phone = phone,
        profilePictureUrl = profilePictureUrl,
        role = UserRole.valueOf(role),
        reputation = Reputation(
            points = points,
            level = UserLevel.fromPoints(points), // Calculado dinámicamente
            badges = badges
        ),
        stats = UserStats(
            activePosts = activePosts,
            finishedPosts = finishedPosts,
            pendingVerification = pendingVerification,
            totalReviews = totalReviews
        )
    )

    companion object {
        fun fromDomain(user: User) = UserDto(
            fullName = user.fullName,
            username = user.username,
            email = user.email,
            phone = user.phone,
            profilePictureUrl = user.profilePictureUrl,
            role = user.role.name,
            points = user.reputation.points,
            level = user.reputation.level.name,
            badges = user.reputation.badges,
            activePosts = user.stats.activePosts,
            finishedPosts = user.stats.finishedPosts,
            pendingVerification = user.stats.pendingVerification,
            totalReviews = user.stats.totalReviews
        )
    }
}
