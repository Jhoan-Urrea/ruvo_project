package com.example.ruvo_app.features.service

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.Locale
import coil.compose.AsyncImage
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    onBackClick: () -> Unit,
    viewModel: CrearServicioViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Hogar") }
    var descripcion by remember { mutableStateOf("") }
    var precioMin by remember { mutableStateOf("") }
    var precioMax by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var radio by remember { mutableStateOf("5") }
    
    val uploadedImages by viewModel.uploadedImages.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    
    val titleError by viewModel.titleError.collectAsState()
    val priceError by viewModel.priceError.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSuccessMessage()
            onBackClick()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) galleryLauncher.launch("image/*")
        else Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
    }

    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(it.latitude, it.longitude, 1) { addresses ->
                                if (addresses.isNotEmpty()) {
                                    ubicacion = addresses[0].getAddressLine(0)
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                ubicacion = addresses[0].getAddressLine(0)
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Location", "Error de seguridad: ${e.message}")
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crear servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { 
                    val min = precioMin.toDoubleOrNull() ?: 0.0
                    val max = precioMax.toDoubleOrNull() ?: 0.0
                    val rad = radio.toDoubleOrNull() ?: 5.0
                    
                    if (titulo.isBlank() || descripcion.isBlank() || ubicacion.isBlank()) {
                        Toast.makeText(context, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.saveServicePost(
                        titulo, categoria, descripcion, min, max, ubicacion, rad,
                        onSuccess = { /* Handled by LaunchedEffect */ }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047FF)),
                enabled = !isSaving && !isUploading
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Publicar servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            FieldLabel("Titulo del servicio *")
            OutlinedTextField(
                value = titulo,
                onValueChange = { 
                    titulo = it
                    viewModel.onTitleChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. Plomero profesional") },
                shape = RoundedCornerShape(12.dp),
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Categoria *")
            OutlinedTextField(
                value = categoria,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                shape = RoundedCornerShape(12.dp),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Descripción *")
            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= 500) descripcion = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("Describe tu servicio, experiencia, disponibilidad, etc.") },
                shape = RoundedCornerShape(12.dp)
            )
            Text("${descripcion.length}/500 caracteres", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Rango de precio estimado *")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = precioMin,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            precioMin = it
                            viewModel.onPricesChanged(it, precioMax)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$ Mínimo") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError != null
                )
                OutlinedTextField(
                    value = precioMax,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            precioMax = it
                            viewModel.onPricesChanged(precioMin, it)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$ Máximo") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError != null
                )
            }
            priceError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Ubicación *")
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color.Gray) },
                placeholder = { Text("Ingresa la dirección o zona") },
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                "Usar mi dirección actual", 
                color = Color(0xFF0047FF), 
                fontSize = 12.sp, 
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { 
                        locationPermissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Radio de cobertura (km)*")
            OutlinedTextField(
                value = radio,
                onValueChange = { if (it.all { char -> char.isDigit() }) radio = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Imágenes del servicio (Mín. 1, Máx. 3) *")
            Text("Toca una imagen para seleccionarla como principal", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uploadedImages) { index, image ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (image.isPrimary) 3.dp else 1.dp,
                                color = if (image.isPrimary) Color(0xFF0047FF) else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectPrimaryImage(index) }
                    ) {
                        AsyncImage(
                            model = image.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (image.isPrimary) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Principal",
                                tint = Color(0xFF0047FF),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(4.dp)
                                    .size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.removeImage(index) },
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                if (uploadedImages.size < 3) {
                    item {
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { 
                                    val status = ContextCompat.checkSelfPermission(context, galleryPermission)
                                    if (status == PackageManager.PERMISSION_GRANTED) {
                                        galleryLauncher.launch("image/*")
                                    } else {
                                        galleryPermissionLauncher.launch(galleryPermission)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isUploading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Add, null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text, 
        fontWeight = FontWeight.Bold, 
        fontSize = 14.sp, 
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}
