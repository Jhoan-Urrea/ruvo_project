package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.ChatMessage
import com.example.ruvo_app.domain.model.Conversation
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
    fun getMessages(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>>
    fun getConversations(currentUserId: String): Flow<List<Conversation>>
    suspend fun markAsRead(conversationId: String, currentUserId: String): Result<Unit>
}
