package com.example.ruvo_app.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.res.stringResource
import com.example.ruvo_app.R

@Composable
fun NotificationsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val notifications = remember {
        mutableStateListOf(
            Notification(
                id = 1,
                title = context.getString(R.string.notification_new_service),
                description = context.getString(R.string.notification_new_service_desc),
                time = context.getString(R.string.notification_time_ago, "7 horas"),
                icon = Icons.Outlined.Notifications,
                iconBackground = Color(0xFFE8EAF6),
                isRead = false
            ),
            Notification(
                id = 2,
                title = context.getString(R.string.notification_new_comment),
                description = context.getString(R.string.notification_new_comment_desc, "Maria Gonzales"),
                time = context.getString(R.string.notification_time_ago, "3 minutos"),
                icon = Icons.Outlined.ChatBubbleOutline,
                iconBackground = Color(0xFFE8F5E9),
                isRead = false
            ),
            Notification(
                id = 3,
                title = context.getString(R.string.notification_service_verified),
                description = context.getString(R.string.notification_service_verified_desc, "Asesor en contabilidad"),
                time = context.getString(R.string.notification_time_ago, "3 minutos"),
                icon = Icons.Outlined.TaskAlt,
                iconBackground = Color(0xFFE3F2FD),
                isRead = false
            ),
            Notification(
                id = 4,
                title = context.getString(R.string.notification_achievement),
                description = context.getString(R.string.notification_achievement_desc, "Intermedio"),
                time = context.getString(R.string.notification_time_ago, "3 minutos"),
                icon = Icons.Outlined.EmojiEvents,
                iconBackground = Color(0xFFFFF3E0),
                isRead = false
            )
        )
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Text(
            text = stringResource(R.string.notifications_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Sub-header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

            if (notifications.any { !it.isRead }) {
                TextButton(onClick = {
                    notifications.forEachIndexed { index, notification ->
                        if (!notification.isRead) {
                            notifications[index] = notification.copy(isRead = true)
                        }
                    }
                }) {
                    Text(
                        text = stringResource(R.string.notifications_mark_all_read),
                        color = Color(0xFF3F51B5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        val index = notifications.indexOf(notification)
                        if (index != -1) {
                            notifications[index] = notification.copy(isRead = true)
                        }
                    }
                )
            }
            
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
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
                    .background(notification.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = Color(0xFF3F51B5), // Using a blue shade for icons as per image
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.Black
                )
                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
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

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}
