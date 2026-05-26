package com.example.ruvo_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageResource(
    val url: String = "",
    val publicId: String = "",
    val isPrimary: Boolean = false
) {
    constructor() : this("", "", false)
}
