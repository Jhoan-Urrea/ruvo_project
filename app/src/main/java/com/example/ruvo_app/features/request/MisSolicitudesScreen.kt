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
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.domain.model.RequestStatus
import com.example.ruvo_app.domain.model.ServiceRequest

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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mis Solicitudes", fontWeight = FontWeight.Bold) },
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
                    }
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentList) { solicitud ->
                            SolicitudItem(
                                solicitud = solicitud,
                                isProviderView = selectedTab == 1,
                                onAceptar = { viewModel.responderSolicitud(solicitud, RequestStatus.ACEPTADA) },
                                onRechazar = { viewModel.responderSolicitud(solicitud, RequestStatus.RECHAZADA) },
                                onCancelar = { viewModel.cancelarSolicitud(solicitud) }
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
    onCancelar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.serviceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = solicitud.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isProviderView) "Cliente: ${solicitud.customerName}" else "Proveedor: ${solicitud.providerName}",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(text = "Fecha: ${solicitud.date} a las ${solicitud.time}", color = Color.Gray, fontSize = 12.sp)
            Text(text = "Ubicación: ${solicitud.location}", color = Color.Gray, fontSize = 12.sp)
            
            if (solicitud.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Detalles adicionales:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = solicitud.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Acciones según la vista y el estado
            if (solicitud.status == RequestStatus.PENDIENTE) {
                Spacer(modifier = Modifier.height(16.dp))
                if (isProviderView) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rechazar")
                        }
                        Button(
                            onClick = onAceptar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aceptar")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onCancelar,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar Solicitud")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
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
