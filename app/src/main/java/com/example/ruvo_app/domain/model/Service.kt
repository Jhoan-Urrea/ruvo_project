package com.example.ruvo_app.domain.model

data class Service(
    val id: String = "",
    val title: String = "",
    val authorName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "",
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val imageUrl: String? = null,
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val status: ServiceStatus = ServiceStatus.ACTIVE
) {
    constructor() : this("")
}

enum class ServiceStatus {
    ACTIVE,
    PENDING,
    INACTIVE,
    FINISHED
}
