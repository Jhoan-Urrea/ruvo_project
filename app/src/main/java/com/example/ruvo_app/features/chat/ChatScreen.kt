package com.example.ruvo_app.features.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.domain.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatData: Screen.Chat,
    onBack: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatData.providerId) {
        viewModel.loadMessages(chatData.providerId)
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chatData.providerImageUrl != null) {
                                AsyncImage(
                                    model = chatData.providerImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = chatData.providerImageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chatData.providerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = chatData.providerSpecialty,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "En linea",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2ECC71)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Opciones */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ChatInputBar(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(chatData.providerId, messageText)
                        messageText = ""
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (chatData.serviceTitle != null) {
                    item {
                        ServiceContextCard(
                            title = chatData.serviceTitle,
                            description = chatData.serviceDescription ?: ""
                        )
                    }
                }

                itemsIndexed(messages) { index, message ->
                    // Show date header if it's the first message or date changed
                    if (shouldShowDateHeader(index, messages)) {
                        DateHeader(timestamp = message.timestamp)
                    }

                    ChatBubble(
                        message = message,
                        isFromMe = message.senderId == currentUserId,
                        showAvatar = shouldShowAvatar(index, messages, currentUserId)
                    )
                }
            }
        }
    }
}

private fun shouldShowDateHeader(index: Int, messages: List<ChatMessage>): Boolean {
    if (index == 0) return true
    val currentMsgDate = Calendar.getInstance().apply { timeInMillis = messages[index].timestamp }
    val prevMsgDate = Calendar.getInstance().apply { timeInMillis = messages[index - 1].timestamp }
    
    return currentMsgDate.get(Calendar.DAY_OF_YEAR) != prevMsgDate.get(Calendar.DAY_OF_YEAR) ||
           currentMsgDate.get(Calendar.YEAR) != prevMsgDate.get(Calendar.YEAR)
}

private fun shouldShowAvatar(index: Int, messages: List<ChatMessage>, currentUserId: String): Boolean {
    // Show avatar only for the last message in a group from the other user
    if (messages[index].senderId == currentUserId) return false
    if (index == messages.size - 1) return true
    return messages[index + 1].senderId != messages[index].senderId
}

@Composable
fun DateHeader(timestamp: Long) {
    val dateText = remember(timestamp) {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = timestamp
        val messageDay = calendar.get(Calendar.DAY_OF_YEAR)
        
        when {
            today == messageDay -> "Hoy"
            today - messageDay == 1 -> "Ayer"
            else -> SimpleDateFormat("dd 'de' MMMM", Locale("es", "ES")).format(Date(timestamp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.LightGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ServiceContextCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isFromMe: Boolean,
    showAvatar: Boolean = false
) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = sdf.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe && showAvatar) {
            // Space for avatar or placeholder can be added here
            Spacer(modifier = Modifier.width(4.dp))
        } else if (!isFromMe) {
            Spacer(modifier = Modifier.width(4.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) Color(0xFF0047FF) else Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromMe) Color.White else Color.Black
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (message.isRead) Color(0xFF80D8FF) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                placeholder = { Text("Escribe tu mensaje", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF0047FF), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}
