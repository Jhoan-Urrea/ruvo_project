package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.NotificationType
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override suspend fun sendNotification(notification: Notification): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document()
                .set(NotificationDto.fromDomain(notification))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = firestore.collection("notifications")
            .whereEqualTo("receiverId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NotificationDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class NotificationDto(
    val receiverId: String = "",
    val type: String = "ESTADO_ACTUALIZADO",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
) {
    fun toDomain(id: String) = Notification(
        id = id,
        receiverId = receiverId,
        type = try { NotificationType.valueOf(type) } catch(e: Exception) { NotificationType.ESTADO_ACTUALIZADO },
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )

    companion object {
        fun fromDomain(n: Notification) = NotificationDto(
            receiverId = n.receiverId,
            type = n.type.name,
            message = n.message,
            timestamp = n.timestamp,
            isRead = n.isRead
        )
    }
}
