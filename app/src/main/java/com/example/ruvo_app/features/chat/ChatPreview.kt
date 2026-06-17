package com.example.ruvo_app.features.chat

data class ChatPreview(
    val id: String,
    val userName: String,
    val userRole: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val imageRes: Int,
    val imageUrl: String? = null
)
