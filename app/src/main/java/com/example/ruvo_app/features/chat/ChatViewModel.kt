package com.example.ruvo_app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ChatMessage
import com.example.ruvo_app.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun loadMessages(otherUserId: String) {
        if (currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            // Mark conversation as read when entering
            val convId = getConversationId(currentUserId, otherUserId)
            chatRepository.markAsRead(convId, currentUserId)
            
            chatRepository.getMessages(currentUserId, otherUserId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun sendMessage(receiverId: String, content: String) {
        if (content.isBlank() || currentUserId.isEmpty()) return
        
        val message = ChatMessage(
            senderId = currentUserId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            chatRepository.sendMessage(message)
        }
    }

    private fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}
