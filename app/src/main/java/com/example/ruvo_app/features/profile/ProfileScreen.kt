package com.example.ruvo_app.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ruvo_app.R
import com.example.ruvo_app.core.component.ServicePostCard
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.User
import java.util.Locale

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onSolicitudesClick: () -> Unit = {},
    onMisTrabajosClick: () -> Unit = {},
    onServiceClick: (ServicePost) -> Unit = {},
    viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProfileUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            is ProfileUiState.Success -> {
                ProfileContent(
                    user = state.user,
                    services = state.services,
                    scrollState = scrollState,
                    onSettingsClick = onSettingsClick,
                    onSolicitudesClick = onSolicitudesClick,
                    onMisTrabajosClick = onMisTrabajosClick,
                    onServiceClick = onServiceClick,
                    onArchiveClick = { viewModel.archiveService(it) },
                    onReactivateClick = { viewModel.reactivateService(it) }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User, 
    services: List<ServicePost>,
    scrollState: androidx.compose.foundation.ScrollState,
    onSettingsClick: () -> Unit,
    onSolicitudesClick: () -> Unit,
    onMisTrabajosClick: () -> Unit,
    onServiceClick: (ServicePost) -> Unit,
    onArchiveClick: (String) -> Unit,
    onReactivateClick: (String) -> Unit
) {
    var selectedServiceTab by remember { mutableIntStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
    ) {
        // Blue Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(48.dp))
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.profile_settings), tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profilePictureUrl != null) {
                            AsyncImage(
                                model = user.profilePictureUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo_ruvo),
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(40.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                        Text(
                            text = user.location?.address ?: "Ubicación no definida",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }

        // Stats and Content
        Column(
            modifier = Modifier
                .offset(y = (-20).dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            // Level Card
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏆", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stringResource(R.string.profile_level)}: ${user.reputation.level.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        val pointsNeeded = user.reputation.getPointsNeededForNextLevel()
                        Text(
                            text = if (pointsNeeded > 0) "Faltan $pointsNeeded XP" else "Nivel Máximo",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { user.reputation.getProgressToNextLevel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${user.reputation.points} XP acumulados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons for Provider
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSolicitudesClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4FF), contentColor = Color(0xFF0047FF))
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solicitudes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onMisTrabajosClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.WorkOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mis Trabajos", fontSize = 12.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Totals Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(icon = Icons.Outlined.ChatBubbleOutline, count = "${services.size}", label = stringResource(R.string.profile_total_services), iconColor = Color(0xFF4CAF50))
                StatItem(icon = Icons.Outlined.FavoriteBorder, count = "${user.stats.totalReviews}", label = "Votos", iconColor = Color(0xFFE91E63))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Estado de servicios
            Text(
                text = stringResource(R.string.profile_service_status),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                StatusRow(icon = Icons.Default.CheckCircleOutline, label = stringResource(R.string.profile_active), count = "${user.stats.activePosts}", color = Color(0xFF4CAF50))
                StatusRow(icon = Icons.Default.AccessTime, label = stringResource(R.string.profile_pending), count = "${user.stats.pendingVerification}", color = Color(0xFFFFC107))
                StatusRow(icon = Icons.Default.Inventory2, label = stringResource(R.string.profile_finished), count = "${user.stats.finishedPosts}", color = Color(0xFF9C27B0))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logros e insignias
            Text(
                text = stringResource(R.string.profile_achievements),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
            ) {
                if (user.reputation.badges.isEmpty()) {
                    item {
                        Text("Aún no tienes insignias", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 20.dp))
                    }
                } else {
                    items(user.reputation.badges) { badge ->
                        AchievementItem(badge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.profile_my_services),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedServiceTab,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Tab(
                    selected = selectedServiceTab == 0,
                    onClick = { selectedServiceTab = 0 },
                    text = { Text("Activos", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedServiceTab == 1,
                    onClick = { selectedServiceTab = 1 },
                    text = { Text("Archivados", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // List of real services from Firestore
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val filteredServices = if (selectedServiceTab == 0) {
                    services.filter { it.status != PostStatus.ARCHIVADO }
                } else {
                    services.filter { it.status == PostStatus.ARCHIVADO }
                }

                if (filteredServices.isEmpty()) {
                    Text(
                        text = if (selectedServiceTab == 0) "No tienes servicios activos" else "No tienes servicios archivados",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    filteredServices.forEach { post ->
                        ServicePostCard(
                            post = post,
                            authorName = user.fullName,
                            authorRole = user.reputation.level.name,
                            showOptions = true,
                            onArchive = { onArchiveClick(post.id) },
                            onReactivate = { onReactivateClick(post.id) },
                            onClick = { onServiceClick(post) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, count: String, label: String, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
    }
}

@Composable
fun StatusRow(icon: ImageVector, label: String, count: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(text = count, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun AchievementItem(title: String) {
    val (emoji, bgColor) = when (title) {
        "Bienvenido a Ruvo" -> "🏅" to Color(0xFFE8F5E9)
        "Primer Contacto" -> "💬" to Color(0xFFE3F2FD)
        "Emprendedor" -> "🚀" to Color(0xFFFFF3E0)
        "Popular" -> "✨" to Color(0xFFF3E5F5)
        "Mano de Obra" -> "🛠️" to Color(0xFFEFEBE9)
        "Explorador" -> "🔍" to Color(0xFFF1F8E9)
        "Crítico" -> "⭐" to Color(0xFFFFFDE7)
        else -> "🎯" to Color(0xFFF5F5F5)
    }

    Surface(
        modifier = Modifier.size(width = 100.dp, height = 120.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp,
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = Color.DarkGray
            )
        }
    }
}
