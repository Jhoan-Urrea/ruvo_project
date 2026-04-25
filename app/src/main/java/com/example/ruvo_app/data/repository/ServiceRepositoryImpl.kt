package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.ImageResource
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServiceCategory
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.GeoPoint
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ServiceRepository {

    override suspend fun saveServicePost(post: ServicePost): Result<Unit> {
        return try {
            val docRef = if (post.id.isEmpty()) {
                firestore.collection("services_posts").document()
            } else {
                firestore.collection("services_posts").document(post.id)
            }
            
            val postWithId = if (post.id.isEmpty()) post.copy(id = docRef.id) else post
            
            docRef.set(ServicePostDto.fromDomain(postWithId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServicePosts(): Flow<List<ServicePost>> = callbackFlow {
        val subscription = firestore.collection("services_posts")
            .whereEqualTo("status", PostStatus.VERIFICADO.name) // Solo verificados
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
}

data class ServicePostDto(
    val authorId: String = "",
    val title: String = "",
    val category: String = "HOGAR",
    val description: String = "",
    val images: List<ImageResourceDto> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val addressText: String = "",
    val coverageRadius: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val status: String = "PENDIENTE",
    val rejectionReason: String? = null,
    val importantCount: Int = 0,
    val isFeatured: Boolean = false
) {
    fun toDomain(id: String) = ServicePost(
        id = id,
        authorId = authorId,
        title = title,
        category = try { ServiceCategory.valueOf(category) } catch(e: Exception) { ServiceCategory.HOGAR },
        description = description,
        images = images.map { it.toDomain() },
        coordinates = GeoPoint(latitude, longitude),
        addressText = addressText,
        coverageRadius = coverageRadius,
        minPrice = minPrice,
        maxPrice = maxPrice,
        status = try { PostStatus.valueOf(status) } catch(e: Exception) { PostStatus.PENDIENTE },
        rejectionReason = rejectionReason,
        importantCount = importantCount,
        isFeatured = isFeatured
    )

    companion object {
        fun fromDomain(post: ServicePost) = ServicePostDto(
            authorId = post.authorId,
            title = post.title,
            category = post.category.name,
            description = post.description,
            images = post.images.map { ImageResourceDto.fromDomain(it) },
            latitude = post.coordinates.latitude,
            longitude = post.coordinates.longitude,
            addressText = post.addressText,
            coverageRadius = post.coverageRadius,
            minPrice = post.minPrice,
            maxPrice = post.maxPrice,
            status = post.status.name,
            rejectionReason = post.rejectionReason,
            importantCount = post.importantCount,
            isFeatured = post.isFeatured
        )
    }
}

data class ImageResourceDto(
    val url: String = "",
    val publicId: String = "",
    val isPrimary: Boolean = false
) {
    fun toDomain() = ImageResource(url, publicId, isPrimary)
    companion object {
        fun fromDomain(res: ImageResource) = ImageResourceDto(res.url, res.publicId, res.isPrimary)
    }
}
