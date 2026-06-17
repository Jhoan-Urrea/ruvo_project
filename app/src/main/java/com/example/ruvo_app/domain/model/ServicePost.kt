package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServicePost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfilePictureUrl: String? = null,
    val title: String = "",
    val category: ServiceCategory = ServiceCategory.HOGAR,
    val description: String = "",
    val images: List<ImageResource> = emptyList(),
    val coordinates: GeoPoint = GeoPoint(0.0, 0.0),
    val addressText: String = "",
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val exactAddress: String = "",
    val coverageRadius: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
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
    val aiFeedbackUseful: Boolean? = null,

    val importantCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this(id = "")
}

enum class ServiceCategory {
    HOGAR, EDUCACION, MASCOTAS, TECNOLOGIA, TRANSPORTE
}

enum class PostStatus {
    PENDIENTE, REVISION_MANUAL, REVISION_MANUAL_PRIORITARIA, VERIFICADO, RECHAZADO, RESUELTO_FINALIZADO, ARCHIVADO
}

@Serializable
data class GeoPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
