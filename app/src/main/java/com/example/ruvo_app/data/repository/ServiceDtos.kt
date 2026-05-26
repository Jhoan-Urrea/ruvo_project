package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.*

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
    fun toDomain(docId: String): ServicePost = ServicePost(
        id = docId,
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
            id = post.id,
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
