package com.example.ruvo_app.features.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarServicioScreen(
    data: Screen.SolicitarServicio,
    onBack: () -> Unit = {},
    onSendSuccess: () -> Unit = {},
    viewModel: SolicitarServicioViewModel = hiltViewModel()
) {
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf(data.location) }
    var offeredPrice by remember { mutableStateOf("") }
    var urgencia by remember { mutableStateOf("Baja prioridad") }
    var detalles by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showUrgencyMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isFormValid = fecha.isNotBlank() && 
                     hora.isNotBlank() && 
                     ubicacion.isNotBlank() && 
                     offeredPrice.isNotBlank() &&
                     uiState !is SolicitarUiState.Loading

    LaunchedEffect(uiState) {
        if (uiState is SolicitarUiState.Success) {
            onSendSuccess()
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        fecha = date.format(DateTimeFormatter.ofPattern("dd / MM / yyyy", Locale("es", "ES")))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(is24Hour = false)
        
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar hora",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar")
                        }
                        TextButton(onClick = {
                            val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                            hora = selectedTime.format(formatter)
                            showTimePicker = false
                        }) {
                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Solicitar servicio", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                windowInsets = WindowInsets.statusBars
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
            ) {
                // Card Informativa
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = data.serviceImageRes),
                                contentDescription = null,
                                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(data.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(data.serviceCategory, color = Color.Gray, fontSize = 12.sp)
                                Text(data.servicePriceRange, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Formulario
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Detalles de tu solicitud", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fecha *", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                            OutlinedTextField(
                                value = fecha,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                                placeholder = { Text("Seleccionar", fontSize = 14.sp) },
                                shape = RoundedCornerShape(10.dp),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.LightGray
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hora *", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                            OutlinedTextField(
                                value = hora,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                                placeholder = { Text("Seleccionar", fontSize = 14.sp) },
                                shape = RoundedCornerShape(10.dp),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Column {
                        Text("Tu presupuesto (COP) *", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        OutlinedTextField(
                            value = offeredPrice,
                            onValueChange = { if (it.all { c -> c.isDigit() }) offeredPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("¿Cuánto ofreces?", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            prefix = { Text("$ ") }
                        )
                    }

                    Column {
                        Text("Urgencia", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        Box {
                            OutlinedTextField(
                                value = urgencia,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { showUrgencyMenu = true },
                                enabled = false,
                                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
                            )
                            DropdownMenu(expanded = showUrgencyMenu, onDismissRequest = { showUrgencyMenu = false }) {
                                listOf("Baja prioridad", "Prioridad normal", "Urgente").forEach { option ->
                                    DropdownMenuItem(text = { Text(option) }, onClick = { urgencia = option; showUrgencyMenu = false })
                                }
                            }
                        }
                    }

                    Column {
                        Text("Más detalles", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        OutlinedTextField(
                            value = detalles,
                            onValueChange = { if (it.length <= 500) detalles = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text("Ej: Necesito que traiga sus propias herramientas...", color = Color.Gray) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.enviarSolicitud(
                            serviceId = data.serviceId, serviceTitle = data.serviceTitle,
                            providerId = data.providerId, providerName = data.providerName,
                            offeredPrice = offeredPrice.toDoubleOrNull() ?: 0.0,
                            date = fecha, time = hora, location = ubicacion,
                            urgency = urgencia, details = detalles
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (uiState is SolicitarUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Enviar solicitud", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
