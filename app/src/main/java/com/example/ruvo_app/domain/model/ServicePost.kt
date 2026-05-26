package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServicePost(
    val id: String = "",
    val authorId: String,
    val authorName: String = "",
    val authorProfilePictureUrl: String? = null,
    val title: String,
    val category: ServiceCategory,
    val description: String,
    val images: List<ImageResource> = emptyList(),
    val coordinates: GeoPoint,
    val addressText: String,
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val exactAddress: String = "",
    val coverageRadius: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val status: PostStatus = PostStatus.PENDIENTE,
    val rejectionReason: String? = null,
    
    // AI Trust Analytics
    val trustScore: Double = 0.0,
    val trustTextScore: Int = 0,
    val trustImageScore: Int = 0,
    val trustAnalysis: String? = null,
    val trustDetails: Map<String, String> = emptyMap(),
    val aiConfidence: Double = 0.0,
    val riskHighlights: List<String> = emptyList(),
    val aiFeedbackUseful: Boolean? = null, // RLHF: Feedback del moderador

    val importantCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ServiceCategory {
    HOGAR,
    EDUCACION,
    MASCOTAS,
    TECNOLOGIA,
    TRANSPORTE
}

enum class PostStatus {
    PENDIENTE,
    REVISION_MANUAL,
    REVISION_MANUAL_PRIORITARIA,
    VERIFICADO,
    RECHAZADO,
    RESUELTO_FINALIZADO,
    ARCHIVADO
}

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
