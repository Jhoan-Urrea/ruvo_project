package com.example.ruvo_app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadConversations()
    }

    private fun loadConversations() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getConversations(currentUserId).collect { conversations ->
                val previews = conversations.map { conv ->
                    val unread = conv.unreadCount[currentUserId] ?: 0
                    
                    ChatPreview(
                        id = conv.id,
                        userName = conv.otherUserName.ifEmpty { "Usuario" },
                        userRole = conv.otherUserRole,
                        lastMessage = conv.lastMessage,
                        time = formatTimestamp(conv.lastTimestamp),
                        unreadCount = unread,
                        imageRes = com.example.ruvo_app.R.drawable.isotipo, // Default icon
                        imageUrl = conv.otherUserImage.ifEmpty { null } // Real profile picture from Cloudinary
                    )
                }
                _chats.value = previews
                _isLoading.value = false
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
