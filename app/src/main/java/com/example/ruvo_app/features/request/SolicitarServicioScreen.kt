package com.example.ruvo_app.features.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarServicioScreen(
    data: Screen.SolicitarServicio,
    onBack: () -> Unit = {},
    onSendSuccess: () -> Unit = {}
) {
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf(data.location) }
    var urgencia by remember { mutableStateOf("Baja prioridad") }
    var detalles by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showUrgencyMenu by remember { mutableStateOf(false) }

    val isFormValid = fecha.isNotBlank() && hora.isNotBlank() && ubicacion.isNotBlank()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        fecha = date.format(DateTimeFormatter.ofPattern("dd / MM / yyyy"))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Limpiar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F9FA))
        ) {
            // Card del Servicio y Proveedor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = data.serviceImageRes),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Surface(color = Color(0xFFE8EFFF), shape = RoundedCornerShape(4.dp)) {
                                Text(data.serviceCategory, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF0047FF), fontWeight = FontWeight.Bold)
                            }
                            Text(data.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(data.servicePriceRange, color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F3F4))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = data.providerImageRes),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(data.providerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(data.providerSpecialty, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Formulario de Detalles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = Color(0xFF0047FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detalles de la solicitud", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fecha preferida *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            readOnly = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            placeholder = { Text("dd / mm / aaaa", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false,
                            trailingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
                            colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.LightGray, disabledTextColor = Color.Black, disabledTrailingIconColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hora preferida *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hora,
                            onValueChange = { hora = it },
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            placeholder = { Text("-- : --", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ubicación del servicio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, null) },
                    shape = RoundedCornerShape(8.dp)
                )
                Text("Cobertura: 5 km desde $ubicacion", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))
                Text("Urgencia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedTextField(
                        value = urgencia,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showUrgencyMenu = true },
                        enabled = false,
                        leadingIcon = { Icon(Icons.Default.Circle, null, tint = if(urgencia == "Baja prioridad") Color(0xFF2ECC71) else Color(0xFFFFC107), modifier = Modifier.size(12.dp)) },
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.LightGray, disabledTextColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    )
                    DropdownMenu(expanded = showUrgencyMenu, onDismissRequest = { showUrgencyMenu = false }) {
                        DropdownMenuItem(text = { Text("Baja prioridad") }, onClick = { urgencia = "Baja prioridad"; showUrgencyMenu = false })
                        DropdownMenuItem(text = { Text("Prioridad normal") }, onClick = { urgencia = "Prioridad normal"; showUrgencyMenu = false })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Detalles adicionales", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = detalles,
                    onValueChange = { if(it.length <= 500) detalles = it },
                    placeholder = { Text("Describe con más detalle lo que necesitas...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Text("${detalles.length}/500", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
            }

            // Nota sobre el precio
            Surface(
                modifier = Modifier.padding(16.dp),
                color = Color(0xFFE8F4FD),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1E9FF))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(24.dp).background(Color(0xFF0047FF), CircleShape).padding(4.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Sobre el precio", fontWeight = FontWeight.Bold, color = Color(0xFF0047FF))
                        Text("El rango de precio es estimado. El proveedor te enviará una cotización exacta según los detalles de tu solicitud.", fontSize = 12.sp, color = Color(0xFF0047FF))
                    }
                }
            }

            // Botón Enviar
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onSendSuccess,
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0047FF),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        disabledContentColor = Color(0xFF424242) // Gris mucho más oscuro para legibilidad
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar solicitud", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Al enviar, aceptas compartir tus datos con el proveedor", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolicitarServicioScreenPreview() {
    Ruvo_appTheme {
        SolicitarServicioScreen(
            data = Screen.SolicitarServicio(
                serviceId = "1",
                serviceTitle = "Plomero profesional - Reparación de fugas",
                serviceCategory = "Hogar",
                servicePriceRange = "$50.000 - $150.000",
                serviceImageRes = R.drawable.plomero,
                providerName = "Carlos Rodríguez",
                providerSpecialty = "Experto",
                providerImageRes = R.drawable.isotipo,
                location = "Chapinero, Bogotá"
            )
        )
    }
}
