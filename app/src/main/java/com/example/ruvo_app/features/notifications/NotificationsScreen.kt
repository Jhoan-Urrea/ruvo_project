package com.example.ruvo_app.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.R
import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.NotificationType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header con statusBarsPadding para Edge-to-Edge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Sub-header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE8EAF6),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F51B5).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.notifications_unread_count, unreadCount),
                        color = Color(0xFF3F51B5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (unreadCount > 0) {
                TextButton(onClick = { viewModel.markAllAsRead() }) {
                    Text(
                        text = stringResource(R.string.notifications_mark_all_read),
                        color = Color(0xFF3F51B5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.NotificationsOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tienes notificaciones", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    top = 8.dp, 
                    end = 16.dp, 
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteNotification(notification.id)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = Color.White)
                            }
                        }
                    ) {
                        NotificationItem(
                            notification = notification,
                            onClick = { viewModel.markAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val context = LocalContext.current
    val icon = when (notification.type) {
        NotificationType.NUEVA_PUBLICACION_ZONA -> Icons.Outlined.Notifications
        NotificationType.NUEVO_COMENTARIO -> Icons.Outlined.ChatBubbleOutline
        NotificationType.ESTADO_ACTUALIZADO -> Icons.Outlined.TaskAlt
        NotificationType.LOGRO_DESBLOQUEADO -> Icons.Outlined.EmojiEvents
        NotificationType.NUEVA_SOLICITUD -> Icons.Outlined.AssignmentTurnedIn
    }

    val iconBackground = when (notification.type) {
        NotificationType.NUEVA_PUBLICACION_ZONA -> Color(0xFFE8EAF6)
        NotificationType.NUEVO_COMENTARIO -> Color(0xFFE8F5E9)
        NotificationType.ESTADO_ACTUALIZADO -> Color(0xFFE3F2FD)
        NotificationType.LOGRO_DESBLOQUEADO -> Color(0xFFFFF3E0)
        NotificationType.NUEVA_SOLICITUD -> Color(0xFFF3E5F5)
    }

    val timeStr = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    }

    // Resolve localized message
    val displayedMessage = remember(notification.message, notification.messageArgs) {
        val resId = context.resources.getIdentifier(notification.message, "string", context.packageName)
        if (resId != 0) {
            context.getString(resId, *notification.messageArgs.toTypedArray())
        } else {
            notification.message
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F2FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (notification.isRead) Color.LightGray.copy(alpha = 0.5f) else Color(0xFF3F51B5).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayedMessage,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color.Black,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F51B5))
                )
            }
        }
    }
}
