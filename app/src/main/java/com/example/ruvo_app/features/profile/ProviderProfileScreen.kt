package com.example.ruvo_app.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.core.component.ReportDialog
import com.example.ruvo_app.core.component.LogrosSection
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderProfileScreen(
    profileData: Screen.PerfilProveedor,
    onBackClick: () -> Unit,
    onContactClick: (User) -> Unit,
    viewModel: ProviderProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profileData.providerId) {
        viewModel.loadProviderProfile(profileData.providerId)
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { reason: String, desc: String ->
                viewModel.reportUser(reason, desc)
                showReportDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val user = (uiState as? ProviderProfileUiState.Success)?.user
            if (user != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { onContactClick(user) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contactar proveedor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ProviderProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProviderProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                is ProviderProfileUiState.Success -> {
                    ProviderProfileContent(
                        user = state.user,
                        services = state.services,
                        initialImageRes = profileData.imageRes,
                        onBackClick = onBackClick,
                        onReportClick = { showReportDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
fun ProviderProfileContent(
    user: User,
    services: List<ServicePost>,
    initialImageRes: Int,
    onBackClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FA))
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
                    .statusBarsPadding()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onReportClick) {
                        Icon(Icons.Outlined.Report, contentDescription = "Reportar", tint = Color.White.copy(alpha = 0.8f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profilePictureUrl != null) {
                        AsyncImage(
                            model = user.profilePictureUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = initialImageRes),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = user.location?.address ?: "Ubicación no definida", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", user.reputation.rating),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${user.stats.totalReviews} reseñas)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Estadísticas y Servicios
        Column(
            modifier = Modifier
                .offset(y = (-24).dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProviderStatItem(value = "${user.stats.activePosts}", label = "Servicios", color = MaterialTheme.colorScheme.primary)
                ProviderStatItem(value = "${user.stats.finishedPosts}", label = "Completados", color = Color(0xFF2ECC71))
                ProviderStatItem(value = "${user.reputation.points}", label = "Puntos", color = Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN DE LOGROS (INSIGNIAS) - Usando componente compartido
            LogrosSection(badges = user.reputation.badges, title = "Logros Obtenidos", emptyMessage = "Este proveedor aún no ha ganado insignias.")

            Spacer(modifier = Modifier.height(24.dp))
            
            InfoSection(
                title = "ACERCA DE",
                items = listOf(
                    InfoItemData(Icons.Outlined.CalendarMonth, "Miembro desde", "enero de 2025")
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "SERVICIOS OFRECIDOS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                services.forEach { post ->
                    ProviderServiceItem(post)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProviderStatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

data class InfoItemData(val icon: ImageVector, val label: String, val value: String)

@Composable
fun InfoSection(title: String, items: List<InfoItemData>) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray))
        Spacer(modifier = Modifier.height(16.dp))
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(item.label, fontSize = 12.sp, color = Color.Gray)
                    Text(item.value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (index < items.size - 1) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProviderServiceItem(post: ServicePost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(post.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(color = Color(0xFFE8EFFF), shape = RoundedCornerShape(4.dp)) {
                Text(
                    post.category.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
