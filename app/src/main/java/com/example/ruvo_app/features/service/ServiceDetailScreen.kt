package com.example.ruvo_app.features.service

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.BuildConfig
import com.example.ruvo_app.R
import com.example.ruvo_app.core.component.StarRatingBar
import com.example.ruvo_app.core.component.ReportDialog
import com.example.ruvo_app.domain.model.Comment
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.Review
import com.example.ruvo_app.domain.model.ServicePost
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val reviews by viewModel.reviews.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val scrollState = rememberScrollState()
    var commentText by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(postId) {
        viewModel.loadService(postId)
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { reason: String, desc: String ->
                viewModel.reportService(reason, desc)
                showReportDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (uiState is ServiceDetailUiState.Success) {
                ChatInputBar(
                    commentText = commentText,
                    onCommentChange = { commentText = it },
                    onSend = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(postId, commentText)
                            commentText = ""
                        }
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .verticalScroll(scrollState)
                    ) {
                        ServiceHeaderImage(post)

                        Column(modifier = Modifier.padding(20.dp)) {
                            ServiceInfoSection(post)

                            if (post.trustAnalysis != null) {
                                TrustAnalysisCard(post)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            ServiceDescription(post)

                            Spacer(modifier = Modifier.height(24.dp))
                            ServiceLocation(post)

                            Spacer(modifier = Modifier.height(24.dp))
                            PriceCard(post)

                            Spacer(modifier = Modifier.height(24.dp))
                            ProviderSection(author, onViewProfileClick)

                            Spacer(modifier = Modifier.height(24.dp))
                            ActionButtons(post, state.isLiked, viewModel::toggleLike, onSolicitarClick)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CommentsSection(comments)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ReviewsSection(reviews)
                            
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            FloatingTopButtons(onBackClick, uiState, { showReportDialog = true })
        }
    }
}

@Composable
fun CommentsSection(comments: List<Comment>) {
    Text(
        text = "Preguntas y Respuestas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    
    if (comments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay preguntas aún. ¡Sé el primero en preguntar!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        comments.forEach { comment ->
            CommentItem(comment)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(comment.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (comment.authorProfilePictureUrl != null) {
                    AsyncImage(
                        model = comment.authorProfilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ReviewItem(review: Review) {
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(review.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (review.customerProfilePictureUrl != null) {
                    AsyncImage(
                        model = review.customerProfilePictureUrl, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Text(text = dateStr, fontSize = 10.sp, color = Color.Gray)
        }
        if (review.comment.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = review.comment, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
fun TrustAnalysisCard(post: ServicePost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análisis de Confianza RUVO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = { (post.trustScore / 100f).toFloat() },
                    modifier = Modifier.size(40.dp),
                    color = if (post.trustScore > 70) Color(0xFF10B981) else Color(0xFFFBBF24),
                    strokeWidth = 4.dp,
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "${post.trustScore.toInt()}% Nivel de confianza", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(text = "Validado por Moderación Inteligente", fontSize = 11.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = post.trustAnalysis ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    
                    post.trustDetails.forEach { (key, value) ->
                        if (key == "alerta") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = value,
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceHeaderImage(post: ServicePost) {
    val images = post.images.sortedByDescending { it.isPrimary }
    val pagerState = rememberPagerState(pageCount = { images.size })
    
    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
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
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun ServiceInfoSection(post: ServicePost) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = post.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
        if (post.status == PostStatus.VERIFICADO) {
            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(12.dp)) {
                Text("Verificado", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        StarRatingBar(rating = post.rating)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = String.format(Locale.getDefault(), "%.1f", post.rating),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "(${post.reviewsCount} reseñas)",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ServiceDescription(post: ServicePost) {
    Text("Descripción", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = post.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
}

@Composable
fun ServiceLocation(post: ServicePost) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    Text("Ubicación del servicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    
    Spacer(modifier = Modifier.height(12.dp))
    
    // Tarjeta de dirección textual
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dirección registrada", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    text = post.addressText, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = { 
                clipboardManager.setText(AnnotatedString(post.addressText))
            }) {
                Icon(Icons.Default.ContentCopy, "Copiar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Mapa Estático / Vista previa
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .clickable {
                // Al tocar el mapa, abre la app externa para interacción completa
                val uri = Uri.parse("geo:${post.coordinates.latitude},${post.coordinates.longitude}?q=${Uri.encode(post.addressText)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(mapIntent)
            }
    ) {
        val latLng = LatLng(post.coordinates.latitude, post.coordinates.longitude)
        if (post.coordinates.latitude != 0.0) {
            AndroidView(
                factory = { ctx ->
                    MapLibre.getInstance(ctx)
                    MapView(ctx).apply {
                        getMapAsync { map ->
                            map.setStyle(Style.Builder().fromUri("https://tiles.stadiamaps.com/styles/alidade_smooth.json?api_key=${BuildConfig.STADIA_API_KEY}"))
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
                            
                            // Añadir marcador visual
                            map.addMarker(MarkerOptions().position(latLng).title(post.title))
                            
                            // DESACTIVAR GESTOS para evitar interferir con el scroll de la pantalla
                            map.uiSettings.setAllGesturesEnabled(false)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Botón flotante sobre el mapa para indicar que es expandible
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abrir en Maps", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun PriceCard(post: ServicePost) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEEF2FF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Presupuesto estimado", color = Color.Gray, fontSize = 12.sp)
            Text(
                text = "$ ${post.minPrice.toInt()} - $ ${post.maxPrice.toInt()} COP",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ProviderSection(author: com.example.ruvo_app.domain.model.User, onViewProfileClick: (String) -> Unit) {
    Text("Proveedor", fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (author.profilePictureUrl != null) {
                AsyncImage(model = author.profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(author.fullName, fontWeight = FontWeight.Bold)
            Text(author.reputation.level.name, color = Color.Gray, fontSize = 12.sp)
        }
        OutlinedButton(onClick = { onViewProfileClick(author.id) }) {
            Text("Ver perfil", fontSize = 12.sp)
        }
    }
}

@Composable
fun ActionButtons(post: ServicePost, isLiked: Boolean, onLikeToggle: (String) -> Unit, onSolicitarClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(
            onClick = { onLikeToggle(post.id) },
            modifier = Modifier.weight(0.4f).height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = if(isLiked) Color.Red else Color.Gray)
        ) {
            Icon(if(isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
        }
        Button(
            onClick = { onSolicitarClick(post.id) },
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Solicitar servicio", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<Review>) {
    Text("Reseñas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))
    
    if (reviews.isEmpty()) {
        Text("No hay reseñas aún", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        reviews.forEach { ReviewItem(it); Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun ChatInputBar(commentText: String, onCommentChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                placeholder = { Text("Escribe una duda...", color = Color.Gray) },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun FloatingTopButtons(onBack: () -> Unit, uiState: ServiceDetailUiState, onReport: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        
        Row {
            IconButton(
                onClick = {
                    (uiState as? ServiceDetailUiState.Success)?.let {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Mira este servicio en RUVO: ${it.post.title}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir"))
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Compartir")
            }
            IconButton(
                onClick = onReport,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Outlined.Report, contentDescription = "Reportar", tint = Color.Red)
            }
        }
    }
}
