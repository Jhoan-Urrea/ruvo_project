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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp) // Edge-to-Edge: Control manual
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
        // Header Azul Inmersivo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Respeta la barra de estado
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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

        // Estadísticas y Contenido
        Column(
            modifier = Modifier
                .offset(y = (-24).dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(top = 24.dp)
        ) {
            // Nivel Card
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
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

            // Totales
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(icon = Icons.Outlined.ChatBubbleOutline, count = "${services.size}", label = stringResource(R.string.profile_total_services), iconColor = Color(0xFF4CAF50))
                StatItem(icon = Icons.Outlined.FavoriteBorder, count = "${user.stats.totalReviews}", label = "Votos", iconColor = Color(0xFFE91E63))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mis Servicios
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
                            showOptions = true,
                            onArchive = { onArchiveClick(post.id) },
                            onReactivate = { onReactivateClick(post.id) },
                            onClick = { onServiceClick(post) }
                        )
                    }
                }
            }

            // Margen para barra de navegación
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(32.dp))
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
