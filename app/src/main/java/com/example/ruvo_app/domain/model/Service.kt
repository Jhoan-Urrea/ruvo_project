package com.example.ruvo_app.domain.model

data class Service(
    val id: String = "",
    val title: String,
    val authorName: String = "", // Added for Admin view
    val description: String,
    val price: Double,
    val priceUnit: String = "", // e.g., "/hora"
    val rating: Float,
    val reviewsCount: Int,
    val imageUrl: String? = null,
    val category: String,
    val location: String,
    val date: String = "", // Added for Admin view
    val status: ServiceStatus = ServiceStatus.ACTIVE
)

enum class ServiceStatus {
    ACTIVE,
    PENDING,
    INACTIVE, // Added for Admin view
    FINISHED
}