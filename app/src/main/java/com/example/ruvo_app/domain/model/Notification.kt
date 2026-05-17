package com.example.ruvo_app.domain.model

data class Notification(
    val id: String = "",
    val receiverId: String,
    val type: NotificationType,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class NotificationType {
    NUEVA_PUBLICACION_ZONA,
    NUEVO_COMENTARIO,
    ESTADO_ACTUALIZADO,
    LOGRO_DESBLOQUEADO
}