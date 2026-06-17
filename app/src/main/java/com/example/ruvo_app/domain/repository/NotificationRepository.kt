package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun sendNotification(notification: Notification): Result<Unit>
    fun getNotifications(userId: String): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
}
