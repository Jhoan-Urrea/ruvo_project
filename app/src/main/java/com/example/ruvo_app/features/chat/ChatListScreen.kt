package com.example.ruvo_app.features.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onBackClick: () -> Unit,
    onChatClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val mockChats = listOf(
        ChatPreview(
            id = "1",
            userName = "Juan Hurtado",
            userRole = "Licenciado",
            lastMessage = "Hola, estoy interesado en tu servicio de tutoría.",
            time = "10:30 AM",
            unreadCount = 2,
            imageRes = R.drawable.isotipo
        ),
        ChatPreview(
            id = "2",
            userName = "Maria Garcia",
            userRole = "Proveedora",
            lastMessage = "¿Podemos agendar para mañana a las 3?",
            time = "Ayer",
            unreadCount = 0,
            imageRes = R.drawable.isotipo
        ),
        ChatPreview(
            id = "3",
            userName = "Pedro Sanchez",
            userRole = "Cliente",
            lastMessage = "Muchas gracias por el servicio, quedó excelente.",
            time = "Lunes",
            unreadCount = 0,
            imageRes = R.drawable.isotipo
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mensajería", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar mensajes...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFF0F0F0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedContainerColor = Color(0xFFF8F9FA)
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(mockChats) { chat ->
                    ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(56.dp)) {
            Image(
                painter = painterResource(id = chat.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop
            )
            // Online status dot (optional)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color(0xFF2ECC71), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = chat.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (chat.unreadCount > 0) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (chat.unreadCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = chat.unreadCount.toString())
                    }
                }
            }
        }
    }
}

data class ChatPreview(
    val id: String,
    val userName: String,
    val userRole: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val imageRes: Int
)

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    Ruvo_appTheme {
        ChatListScreen(onBackClick = {}, onChatClick = {})
    }
}
