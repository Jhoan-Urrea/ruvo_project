package com.example.ruvo_app.features.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.core.component.StarRatingBar
import com.example.ruvo_app.domain.model.RequestStatus
import com.example.ruvo_app.domain.model.ServiceRequest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    onBack: () -> Unit,
    viewModel: SolicitudesViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Mis Pedidos", "Trabajos Recibidos")

    val recibidas by viewModel.solicitudesRecibidas.collectAsState()
    val enviadas by viewModel.solicitudesEnviadas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showReviewDialog by remember { mutableStateOf<ServiceRequest?>(null) }

    if (showReviewDialog != null) {
        CalificarServiceDialog(
            request = showReviewDialog!!,
            onDismiss = { showReviewDialog = null },
            onSubmit = { rating: Int, comment: String ->
                viewModel.calificarServicio(showReviewDialog!!, rating, comment)
                showReviewDialog = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.statusBarsPadding()) { 
                    TopAppBar(
                        title = { Text("Mis Solicitudes", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { 
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val currentList = if (selectedTab == 0) enviadas else recibidas
                
                if (currentList.isEmpty()) {
                    EmptyState(if (selectedTab == 0) "No has realizado solicitudes aún" else "No has recibido trabajos todavía")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, 
                            top = 16.dp, 
                            end = 16.dp, 
                            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentList, key = { it.id }) { solicitud ->
                            SolicitudItem(
                                solicitud = solicitud,
                                isProviderView = selectedTab == 1,
                                onAceptar = { viewModel.responderSolicitud(solicitud, RequestStatus.ACEPTADA) },
                                onRechazar = { viewModel.responderSolicitud(solicitud, RequestStatus.RECHAZADA) },
                                onFinalizar = { viewModel.responderSolicitud(solicitud, RequestStatus.COMPLETADA) },
                                onCancelar = { viewModel.cancelarSolicitud(solicitud) },
                                onCalificar = { showReviewDialog = solicitud }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudItem(
    solicitud: ServiceRequest,
    isProviderView: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onFinalizar: () -> Unit,
    onCancelar: () -> Unit,
    onCalificar: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = solicitud.serviceTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = if (isProviderView) "Solicitado por: ${solicitud.customerName}" else "Proveedor: ${solicitud.providerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                StatusBadge(status = solicitud.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Presupuesto", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = currencyFormat.format(solicitud.offeredPrice),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Fecha programada", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = "${solicitud.date} - ${solicitud.time}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }

            if (solicitud.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF5F7FA),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = solicitud.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACCIONES PROMINENTES
            when (solicitud.status) {
                RequestStatus.PENDIENTE -> {
                    if (isProviderView) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRechazar,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE74C3C)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Rechazar", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onAceptar,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Aceptar", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCancelar,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancelar Solicitud")
                        }
                    }
                }
                RequestStatus.ACEPTADA -> {
                    if (isProviderView) {
                        Button(
                            onClick = onFinalizar,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Finalizar Trabajo", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "El proveedor ha aceptado. ¡Ponte en contacto pronto!",
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF2E7D32),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                RequestStatus.COMPLETADA -> {
                    if (!isProviderView) {
                        Button(
                            onClick = onCalificar,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calificar servicio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message, 
                color = Color.Gray, 
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun CalificarServiceDialog(
    request: ServiceRequest,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar Servicio", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "¿Cómo calificarías el trabajo de ${request.providerName}?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                StarRatingBar(
                    rating = rating.toFloat(),
                    selectable = true,
                    onRatingChanged = { rating = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Escribe un comentario opcional...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Enviar calificación")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StatusBadge(status: RequestStatus) {
    val color = when (status) {
        RequestStatus.PENDIENTE -> Color(0xFFFFC107)
        RequestStatus.ACEPTADA -> Color(0xFF2ECC71)
        RequestStatus.RECHAZADA -> Color(0xFFE74C3C)
        RequestStatus.CANCELADA -> Color(0xFF95A5A6)
        RequestStatus.COMPLETADA -> Color(0xFF3498DB)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
