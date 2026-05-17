package com.example.ruvo_app.domain.service

import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.NotificationType
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationService @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend fun checkAndAwardAchievement(userId: String, achievement: Achievement) {
        val userResult = userRepository.getUserProfile(userId).first()
        val user = userResult.getOrNull() ?: return

        // Si el usuario ya tiene la medalla, no hacemos nada
        if (user.reputation.badges.contains(achievement.badgeName)) return

        // Otorgar puntos y medalla
        userRepository.addUserPoints(userId, achievement.xpReward)
        
        val updatedBadges = user.reputation.badges + achievement.badgeName
        userRepository.updateUserProfile(userId, mapOf("badges" to updatedBadges))

        // Notificar al usuario
        notificationRepository.sendNotification(
            Notification(
                receiverId = userId,
                type = NotificationType.LOGRO_DESBLOQUEADO,
                message = "🏆 ¡Logro desbloqueado! '${achievement.badgeName}': ${achievement.description} (+${achievement.xpReward} XP)"
            )
        )
    }
}

sealed class Achievement(val badgeName: String, val description: String, val xpReward: Int) {
    object Bienvenido : Achievement("Bienvenido a Ruvo", "¡Ya eres parte de la comunidad!", 10)
    object PrimerContacto : Achievement("Primer Contacto", "Iniciaste tu primera conversación", 10)
    object Emprendedor : Achievement("Emprendedor", "Publicaste tu primer servicio", 50)
    object Popular : Achievement("Popular", "Tu servicio alcanzó 10 likes", 100)
    object ManoDeObra : Achievement("Mano de Obra", "Completaste tu primer trabajo", 150)
    object Explorador : Achievement("Explorador", "Enviaste tu primera solicitud", 30)
    object Critico : Achievement("Crítico", "Dejaste tu primer comentario en un servicio", 20)
}
