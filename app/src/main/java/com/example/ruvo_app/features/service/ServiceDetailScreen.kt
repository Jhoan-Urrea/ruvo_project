package com.example.ruvo_app.features.service

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.BuildConfig
import com.example.ruvo_app.R
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServiceDetailScreen(
    postId: String,
    onBackClick: () -> Unit,
    onViewProfileClick: (String) -> Unit = {},
    onSolicitarClick: (String) -> Unit = {},
    viewModel: ServiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadService(postId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del servicio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Compartir */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        placeholder = { Text("Agrega un comentario", color = Color.Gray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { /* TODO: Enviar */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ServiceDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ServiceDetailUiState.Error -> {
                    Text(text = state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is ServiceDetailUiState.Success -> {
                    val post = state.post
                    val author = state.author
                    val isOwner = state.isOwner

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .verticalScroll(scrollState)
                    ) {
                        // Image Pager
                        val images = post.images.sortedByDescending { it.isPrimary }
                        val pagerState = rememberPagerState(pageCount = { images.size })
                        
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = images[page].url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.card_service)
                                )
                            }

                            if (images.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(images.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                    }
                                }
                            }

                            // Badges overlay
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = Color(0xFFE8F0FE).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = post.category.name.lowercase().replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color(0xFF0047FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                
                                if (post.isFeatured) {
                                    Surface(color = Color(0xFF0047FF), shape = RoundedCornerShape(16.dp)) {
                                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Destacado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Owner Management
                            if (isOwner && images.isNotEmpty()) {
                                val currentImage = images[pagerState.currentPage]
                                if (!currentImage.isPrimary) {
                                    Button(
                                        onClick = { viewModel.setPrimaryImage(post.id, currentImage.publicId) },
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Poner como portada", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(text = post.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                if (post.status == PostStatus.VERIFICADO) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(12.dp)) {
                                        Text("Verificado", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = post.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(24.dp))

                            // MAP SECTION
                            Text("Ubicación del servicio", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.LightGray)
                            ) {
                                val latLng = LatLng(post.coordinates.latitude, post.coordinates.longitude)
                                if (post.coordinates.latitude != 0.0) {
                                    AndroidView(
                                        factory = { context ->
                                            MapLibre.getInstance(context)
                                            MapView(context).apply {
                                                getMapAsync { map ->
                                                    map.setStyle(Style.Builder().fromUri("https://tiles.stadiamaps.com/styles/alidade_smooth.json?api_key=${BuildConfig.STADIA_API_KEY}"))
                                                    map.cameraPosition = CameraPosition.Builder().target(latLng).zoom(14.0).build()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = post.addressText, color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Price Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Rango de precio estimado", color = Color.Gray, fontSize = 12.sp)
                                    Text(
                                        text = "$ ${post.minPrice.toInt()} - $ ${post.maxPrice.toInt()} COP",
                                        color = Color(0xFF0047FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Provider section
                            Text("Proveedor del servicio", fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_ruvo),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(author.fullName, fontWeight = FontWeight.Bold)
                                    Text(author.reputation.level.name.lowercase().replaceFirstChar { it.uppercase() }, color = Color.Gray, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { onViewProfileClick(author.id) },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Ver perfil", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleLike(post.id) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, if (state.isLiked) Color(0xFF0047FF) else Color.LightGray)
                                ) {
                                    Icon(
                                        imageVector = if (state.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (state.isLiked) Color(0xFF0047FF) else Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Me interesa (${post.likedBy.size})",
                                        color = if (state.isLiked) Color(0xFF0047FF) else Color.Black
                                    )
                                }
                                Button(
                                    onClick = { onSolicitarClick(post.id) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047FF))
                                ) {
                                    Text("Solicitar servicio", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Comentarios (2)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
