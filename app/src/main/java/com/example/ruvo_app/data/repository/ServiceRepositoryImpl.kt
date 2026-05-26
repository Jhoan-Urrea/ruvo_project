package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ServiceRepository {

    override suspend fun saveServicePost(post: ServicePost): Result<String> {
        return try {
            val docRef = if (post.id.isEmpty()) {
                firestore.collection("services_posts").document()
            } else {
                firestore.collection("services_posts").document(post.id)
            }
            
            val postWithId = if (post.id.isEmpty()) post.copy(id = docRef.id) else post
            
            docRef.set(ServicePostDto.fromDomain(postWithId)).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServicePosts(): Flow<List<ServicePost>> = callbackFlow {
        val subscription = firestore.collection("services_posts")
            .whereEqualTo("status", PostStatus.VERIFICADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServicePostDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    override fun getServicePostsByAuthor(authorId: String): Flow<List<ServicePost>> = callbackFlow {
        val subscription = firestore.collection("services_posts")
            .whereEqualTo("authorId", authorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServicePostDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllServicePosts(): Flow<List<ServicePost>> = callbackFlow {
        val subscription = firestore.collection("services_posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServicePostDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    override fun getServicePostById(postId: String): Flow<ServicePost?> = callbackFlow {
        val subscription = firestore.collection("services_posts").document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val post = snapshot?.toObject(ServicePostDto::class.java)?.toDomain(snapshot.id)
                trySend(post)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updatePostStatus(postId: String, newStatus: PostStatus): Result<Unit> {
        return try {
            firestore.collection("services_posts").document(postId)
                .update("status", newStatus.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTrustAnalysis(
        postId: String, 
        score: Double, 
        textScore: Int,
        imageScore: Int,
        analysis: String, 
        details: Map<String, String>,
        aiConfidence: Double,
        riskHighlights: List<String>,
        newStatus: PostStatus
    ): Result<Unit> {
        return try {
            firestore.collection("services_posts").document(postId)
                .update(
                    mapOf(
                        "trustScore" to score,
                        "trustTextScore" to textScore,
                        "trustImageScore" to imageScore,
                        "trustAnalysis" to analysis,
                        "trustDetails" to details,
                        "aiConfidence" to aiConfidence,
                        "riskHighlights" to riskHighlights,
                        "status" to newStatus.name
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAiFeedback(postId: String, isUseful: Boolean): Result<Unit> {
        return try {
            firestore.collection("services_posts").document(postId)
                .update("aiFeedbackUseful", isUseful)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun archiveService(postId: String): Result<Unit> {
        return updatePostStatus(postId, PostStatus.ARCHIVADO)
    }

    override suspend fun reactivateService(postId: String): Result<Unit> {
        return updatePostStatus(postId, PostStatus.PENDIENTE)
    }

    override suspend fun rejectPost(postId: String, reason: String): Result<Unit> {
        return try {
            firestore.collection("services_posts").document(postId)
                .update(
                    mapOf(
                        "status" to PostStatus.RECHAZADO.name,
                        "rejectionReason" to reason
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(postId: String, userId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("services_posts").document(postId)
            val doc = docRef.get().await()
            val likedBy = doc.get("likedBy") as? List<*> ?: emptyList<String>()

            if (likedBy.contains(userId)) {
                docRef.update("likedBy", FieldValue.arrayRemove(userId)).await()
            } else {
                docRef.update("likedBy", FieldValue.arrayUnion(userId)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAvailableCountries(): Flow<List<String>> {
        return getAllServicePosts().map { posts ->
            posts.map { it.country }.filter { it.isNotEmpty() }.distinct().sorted()
        }
    }

    override fun getAvailableRegions(): Flow<List<String>> {
        return getAllServicePosts().map { posts ->
            posts.map { it.region }.filter { it.isNotEmpty() }.distinct().sorted()
        }
    }

    override fun getAvailableCities(): Flow<List<String>> {
        return getAllServicePosts().map { posts ->
            posts.map { it.city }.filter { it.isNotEmpty() }.distinct().sorted()
        }
    }
}
