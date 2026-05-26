package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.ChatMessage
import com.example.ruvo_app.domain.model.Conversation
import com.example.ruvo_app.domain.repository.ChatRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val convId = getConversationId(message.senderId, message.receiverId)
            val conversationRef = firestore.collection("conversations").document(convId)
            val messageRef = conversationRef.collection("messages").document()

            // Usamos un batch para asegurar atomicidad
            firestore.runBatch { batch ->
                // 1. Datos básicos de la conversación
                val convData = mutableMapOf<String, Any>(
                    "participants" to listOf(message.senderId, message.receiverId),
                    "lastMessage" to message.content,
                    "lastTimestamp" to message.timestamp,
                    "lastSenderId" to message.senderId
                )
                
                // Merge para no sobreescribir metadatos (nombres/fotos) si ya existen
                batch.set(conversationRef, convData, SetOptions.merge())
                
                // 2. Incrementar contador de no leídos para el receptor
                batch.update(conversationRef, "unreadCount.${message.receiverId}", FieldValue.increment(1))
                
                // 3. Guardar el mensaje en la sub-colección
                batch.set(messageRef, message.copy(id = messageRef.id))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Si el error es porque el documento no existe (update falló), forzamos creación
            handleNewConversation(message)
        }
    }

    private suspend fun handleNewConversation(message: ChatMessage): Result<Unit> {
        return try {
            val convId = getConversationId(message.senderId, message.receiverId)
            val conversationRef = firestore.collection("conversations").document(convId)
            
            val initialData = mapOf(
                "participants" to listOf(message.senderId, message.receiverId),
                "lastMessage" to message.content,
                "lastTimestamp" to message.timestamp,
                "lastSenderId" to message.senderId,
                "unreadCount" to mapOf(message.receiverId to 1, message.senderId to 0)
            )
            
            conversationRef.set(initialData, SetOptions.merge()).await()
            val messageRef = conversationRef.collection("messages").document()
            messageRef.set(message.copy(id = messageRef.id)).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(currentUserId: String, otherUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val convId = getConversationId(currentUserId, otherUserId)
        val subscription = firestore.collection("conversations")
            .document(convId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try { doc.toObject(ChatMessage::class.java) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { subscription.remove() }
    }

    override fun getConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val subscription = firestore.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    try { 
                        doc.toObject(Conversation::class.java)?.copy(id = doc.id) 
                    } catch (e: Exception) { null }
                } ?: emptyList()
                
                trySend(conversations)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markAsRead(conversationId: String, currentUserId: String): Result<Unit> {
        return try {
            firestore.collection("conversations").document(conversationId)
                .update("unreadCount.$currentUserId", 0)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
