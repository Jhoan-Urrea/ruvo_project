package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceRequest(
    val id: String = "",
    val serviceId: String,
    val serviceTitle: String,
    val customerId: String,
    val customerName: String,
    val providerId: String,
    val providerName: String,
    val offeredPrice: Double = 0.0,
    val date: String,
    val time: String,
    val location: String,
    val urgency: String,
    val details: String,
    val status: RequestStatus = RequestStatus.PENDIENTE,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RequestStatus {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    CANCELADA,
    COMPLETADA
}
