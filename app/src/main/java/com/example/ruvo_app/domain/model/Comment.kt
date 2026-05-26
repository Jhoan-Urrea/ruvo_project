package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfilePictureUrl: String? = null,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)