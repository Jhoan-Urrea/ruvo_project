package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Map<String, Int> = emptyMap(),
    // Metadata for UI optimization
    val otherUserName: String = "",
    val otherUserRole: String = "",
    val otherUserImage: String = ""
) {
    constructor() : this("", emptyList(), "", 0L, emptyMap(), "", "", "")
}
