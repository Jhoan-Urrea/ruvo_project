package com.example.ruvo_app.domain.model

data class ImageResource(
    val url: String,
    val publicId: String,
    val isPrimary: Boolean = false
)
