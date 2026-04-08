package com.example.ruvo_app.domain.model

data class Comment(
    val id: String = "",
    val postId: String,
    val authorId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)