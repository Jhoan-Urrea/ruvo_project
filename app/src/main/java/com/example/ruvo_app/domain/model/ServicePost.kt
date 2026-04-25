package com.example.ruvo_app.domain.model

data class ServicePost(
    val id: String = "",
    val authorId: String,
    val title: String,
    val category: ServiceCategory,
    val description: String, // Max 500 characters
    val images: List<ImageResource> = emptyList(),
    val coordinates: GeoPoint,
    val addressText: String,
    val coverageRadius: Double, // in kilometers
    val minPrice: Double,
    val maxPrice: Double,
    val status: PostStatus = PostStatus.PENDIENTE,
    val rejectionReason: String? = null,
    val importantCount: Int = 0,
    val isFeatured: Boolean = false
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
    RESUELTO_FINALIZADO
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
