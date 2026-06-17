package com.example.ruvo_app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ChatMessage
import com.example.ruvo_app.domain.repository.ChatRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun loadMessages(otherUserId: String) {
        if (currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            val convId = getConversationId(currentUserId, otherUserId)
            chatRepository.markAsRead(convId, currentUserId)
            
            chatRepository.getMessages(currentUserId, otherUserId)
                .catch { _messages.value = emptyList() }
                .collect { list ->
                    _messages.value = list
                    _isLoading.value = false
                }
        }
    }

    fun sendMessage(receiverId: String, content: String, receiverName: String? = null, receiverImage: String? = null) {
        if (content.isBlank() || currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                // Obtenemos mi perfil para que el otro usuario vea mi nombre en su lista de chats
                val myProfile = userRepository.getUserProfile(currentUserId).first().getOrNull()
                
                val message = ChatMessage(
                    senderId = currentUserId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )
                
                val result = chatRepository.sendMessage(message)
                if (result.isSuccess) {
                    gamificationService.checkAndAwardAchievement(currentUserId, Achievement.PrimerContacto)
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente o notificar al usuario
            }
        }
    }

    private fun getConversationId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }
}
