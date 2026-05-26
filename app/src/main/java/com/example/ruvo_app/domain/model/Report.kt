package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: String = "",
    val reportedId: String, // ID del servicio o usuario reportado
    val reporterId: String, // ID de quien reporta
    val type: ReportType,
    val reason: String,
    val description: String,
    val status: ReportStatus = ReportStatus.PENDIENTE,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNote: String? = null
)

enum class ReportType {
    SERVICIO,
    USUARIO,
    MENSAJE
}

enum class ReportStatus {
    PENDIENTE,
    EN_REVISION,
    RESUELTO,
    DESESTIMADO
}
