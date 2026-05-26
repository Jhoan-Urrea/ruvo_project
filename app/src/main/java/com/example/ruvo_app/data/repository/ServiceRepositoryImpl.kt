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

data class ServicePostDto(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfilePictureUrl: String? = null,
    val title: String = "",
    val category: String = "HOGAR",
    val description: String = "",
    val images: List<ImageResourceDto> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val addressText: String = "",
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val exactAddress: String = "",
    val coverageRadius: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val status: String = "PENDIENTE",
    val rejectionReason: String? = null,
    val trustScore: Double = 0.0,
    val trustTextScore: Int = 0,
    val trustImageScore: Int = 0,
    val trustAnalysis: String? = null,
    val trustDetails: Map<String, String> = emptyMap(),
    val aiConfidence: Double = 0.0,
    val riskHighlights: List<String> = emptyList(),
    val aiFeedbackUseful: Boolean? = null,
    val importantCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val createdAt: Long = 0L
) {
    fun toDomain(id: String) = ServicePost(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorProfilePictureUrl = authorProfilePictureUrl,
        title = title,
        category = try { ServiceCategory.valueOf(category) } catch(e: Exception) { ServiceCategory.HOGAR },
        description = description,
        images = images.map { it.toDomain() },
        coordinates = GeoPoint(latitude, longitude),
        addressText = addressText,
        country = country,
        region = region,
        city = city,
        exactAddress = exactAddress,
        coverageRadius = coverageRadius,
        minPrice = minPrice,
        maxPrice = maxPrice,
        status = try { PostStatus.valueOf(status) } catch(e: Exception) { PostStatus.PENDIENTE },
        rejectionReason = rejectionReason,
        trustScore = trustScore,
        trustTextScore = trustTextScore,
        trustImageScore = trustImageScore,
        trustAnalysis = trustAnalysis,
        trustDetails = trustDetails,
        aiConfidence = aiConfidence,
        riskHighlights = riskHighlights,
        aiFeedbackUseful = aiFeedbackUseful,
        importantCount = importantCount,
        likedBy = likedBy,
        isFeatured = isFeatured,
        rating = rating.toFloat(),
        reviewsCount = reviewsCount,
        createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt
    )

    companion object {
        fun fromDomain(post: ServicePost) = ServicePostDto(
            authorId = post.authorId,
            authorName = post.authorName,
            authorProfilePictureUrl = post.authorProfilePictureUrl,
            title = post.title,
            category = post.category.name,
            description = post.description,
            images = post.images.map { ImageResourceDto.fromDomain(it) },
            latitude = post.coordinates.latitude,
            longitude = post.coordinates.longitude,
            addressText = post.addressText,
            country = post.country,
            region = post.region,
            city = post.city,
            exactAddress = post.exactAddress,
            coverageRadius = post.coverageRadius,
            minPrice = post.minPrice,
            maxPrice = post.maxPrice,
            status = post.status.name,
            rejectionReason = post.rejectionReason,
            trustScore = post.trustScore,
            trustTextScore = post.trustTextScore,
            trustImageScore = post.trustImageScore,
            trustAnalysis = post.trustAnalysis,
            trustDetails = post.trustDetails,
            aiConfidence = post.aiConfidence,
            riskHighlights = post.riskHighlights,
            aiFeedbackUseful = post.aiFeedbackUseful,
            importantCount = post.importantCount,
            likedBy = post.likedBy,
            isFeatured = post.isFeatured,
            rating = post.rating.toDouble(),
            reviewsCount = post.reviewsCount,
            createdAt = post.createdAt
        )
    }
}
