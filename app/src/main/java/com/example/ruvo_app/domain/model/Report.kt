package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: String = "",
    val reportedId: String = "",
    val reporterId: String = "",
    val type: ReportType = ReportType.SERVICIO,
    val reason: String = "",
    val description: String = "",
    val status: ReportStatus = ReportStatus.PENDIENTE,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,
    val resolutionNote: String? = null
) {
    constructor() : this("")
}

enum class ReportType { SERVICIO, USUARIO, MENSAJE }
enum class ReportStatus { PENDIENTE, EN_REVISION, RESUELTO, DESESTIMADO }
