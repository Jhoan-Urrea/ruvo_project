package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String = "",
    val serviceId: String,
    val providerId: String,
    val customerId: String,
    val customerName: String,
    val customerProfilePictureUrl: String? = null,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
