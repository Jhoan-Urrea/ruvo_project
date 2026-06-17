package com.example.ruvo_app.features.notifications

import androidx.compose.ui.graphics.vector.ImageVector

data class Notification(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val iconBackground: androidx.compose.ui.graphics.Color,
    val isRead: Boolean = false
)
