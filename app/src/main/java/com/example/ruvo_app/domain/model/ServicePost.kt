package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServicePost(
    val id: String = "",
    val authorId: String,
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
    val importantCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val isFeatured: Boolean = false,
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
