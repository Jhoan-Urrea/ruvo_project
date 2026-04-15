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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.Locale
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ruvo_app.core.theme.Ruvo_appTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    onBackClick: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Hogar") }
    var descripcion by remember { mutableStateOf("") }
    var precioMin by remember { mutableStateOf("") }
    var precioMax by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var radio by remember { mutableStateOf("5") }
    
    // 6. Estado de imagen
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 1. Launcher para abrir la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImage = it
            isUploading = true
            subirImagenCloudinary(it, 
                onSuccess = { url ->
                    imageUrl = url
                    isUploading = false
                },
                onError = {
                    isUploading = false
                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // 2. Lógica de Permisos para Galería (según normativa Android 13+)
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

    // 3. Lógica de Permisos para Ubicación
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
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            ubicacion = address
                            Toast.makeText(context, "Ubicación cargada", Toast.LENGTH_SHORT).show()
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

    // 4. Lógica de Permisos para Cámara (si decides usarla)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Manejar el bitmap capturado
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
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
                    if (imageUrl.isNotEmpty()) {
                        Toast.makeText(context, "Servicio publicado!", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    } else if (isUploading) {
                        Toast.makeText(context, "Subiendo imagen, espera un momento...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Por favor sube una imagen primero", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047FF))
            ) {
                Text("Publicar servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. Plomero profesional") },
                shape = RoundedCornerShape(12.dp)
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

            FieldLabel("Descipción *")
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
                    onValueChange = { if (it.all { char -> char.isDigit() }) precioMin = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$ Mínimo") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp)) }
                )
                OutlinedTextField(
                    value = precioMax,
                    onValueChange = { if (it.all { char -> char.isDigit() }) precioMax = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("$ Máximo") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp)) }
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
                "Usar mi dirección acctual", 
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }
            )
            Text("¿Hasta qué distancia te desplazas?", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Imágenes del servicio")
            
            // 11. Comportamiento UI (Botón vs Preview)
            if (selectedImage == null) {
                Card(
                    modifier = Modifier
                        .size(120.dp)
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Subiendo...", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            Icon(Icons.Default.Upload, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Text("Subir", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                // 7. Visualización con Coil
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { 
                            // Podrías mostrar un diálogo para elegir entre Cámara o Galería aquí
                            val status = ContextCompat.checkSelfPermission(context, galleryPermission)
                            if (status == PackageManager.PERMISSION_GRANTED) {
                                galleryLauncher.launch("image/*")
                            } else {
                                galleryPermissionLauncher.launch(galleryPermission)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = if (isUploading) 0.5f else 1f
                    )
                    if (isUploading) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            
            Text(
                "Puedes agregar hasta 5 imágenes (opcional)", 
                fontSize = 12.sp, 
                color = Color.Gray, 
                modifier = Modifier.padding(top = 8.dp)
            )

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

// 9. Función de subida a Cloudinary
fun subirImagenCloudinary(uri: Uri, onSuccess: (String) -> Unit, onError: () -> Unit) {
    // 10. Log inicio
    Log.d("Cloudinary", "Iniciando subida de imagen: $uri")
    
    MediaManager.get().upload(uri)
        .unsigned("ruvo_app") // 8. Upload preset unsigned
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                Log.d("Cloudinary", "Subida iniciada con ID: $requestId")
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                Log.d("Cloudinary", "Progreso de subida: $progress%")
            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val secureUrl = resultData["secure_url"] as String
                // 10. Log éxito
                Log.d("Cloudinary", "Subida EXITOSA. URL: $secureUrl")
                onSuccess(secureUrl)
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                // 10. Log error
                Log.e("Cloudinary", "Error en la subida: ${error.description}")
                onError()
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                Log.d("Cloudinary", "Subida reprogramada")
            }
        }).dispatch()
}

@Preview(showBackground = true)
@Composable
fun CrearServicioScreenPreview() {
    Ruvo_appTheme {
        CrearServicioScreen(onBackClick = {})
    }
}
