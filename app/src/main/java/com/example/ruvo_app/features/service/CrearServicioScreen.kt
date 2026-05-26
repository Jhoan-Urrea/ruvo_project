package com.example.ruvo_app.features.service

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.domain.model.ServiceCategory
import com.google.android.gms.location.LocationServices
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearServicioScreen(
    onBackClick: () -> Unit,
    onSelectLocationClick: () -> Unit,
    initialLat: Double? = null,
    initialLng: Double? = null,
    initialAddress: String? = null,
    initialCountry: String? = null,
    initialRegion: String? = null,
    initialCity: String? = null,
    initialExact: String? = null,
    viewModel: CrearServicioViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var categoriaExpander by remember { mutableStateOf(false) }
    var categoriaSeleccionada by remember { mutableStateOf(ServiceCategory.HOGAR) }
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
    val descriptionError by viewModel.descriptionError.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val isFormValid = titulo.isNotBlank() && 
                     descripcion.isNotBlank() && 
                     precioMin.isNotBlank() && 
                     precioMax.isNotBlank() && 
                     ubicacion.isNotBlank() && 
                     uploadedImages.isNotEmpty()

    LaunchedEffect(initialLat, initialLng, initialAddress) {
        if (initialLat != null && initialLng != null && initialAddress != null) {
            viewModel.setLocationData(
                lat = initialLat, lng = initialLng,
                country = initialCountry ?: "", region = initialRegion ?: "",
                city = initialCity ?: "", exact = initialExact ?: ""
            )
            ubicacion = initialAddress
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
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
                            geocoder.getFromLocation(it.latitude, it.longitude, 1, object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    if (addresses.isNotEmpty()) {
                                        val addr = addresses[0]
                                        viewModel.setLocationData(
                                            lat = it.latitude, lng = it.longitude,
                                            country = addr.countryName ?: "", region = addr.adminArea ?: "",
                                            city = addr.locality ?: addr.subAdminArea ?: "", exact = addr.thoroughfare ?: addr.featureName ?: ""
                                        )
                                        ubicacion = addr.getAddressLine(0) ?: ""
                                    }
                                }
                            })
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                viewModel.setLocationData(
                                    lat = it.latitude, lng = it.longitude,
                                    country = addr.countryName ?: "", region = addr.adminArea ?: "",
                                    city = addr.locality ?: addr.subAdminArea ?: "", exact = addr.thoroughfare ?: addr.featureName ?: ""
                                )
                                ubicacion = addr.getAddressLine(0) ?: ""
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("Location", "Error de seguridad: ${e.message}")
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crear servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = { 
                        viewModel.saveServicePost(
                            titulo, categoriaSeleccionada.name, descripcion, 
                            precioMin.toDoubleOrNull() ?: 0.0, 
                            precioMax.toDoubleOrNull() ?: 0.0, 
                            ubicacion, radio.toDoubleOrNull() ?: 5.0,
                            onSuccess = { }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = isFormValid && !isSaving && !isUploading
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Publicar servicio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            FieldLabel("Título del servicio *")
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it; viewModel.onTitleChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. Plomero profesional", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it.asString()) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Categoría *")
            ExposedDropdownMenuBox(
                expanded = categoriaExpander,
                onExpandedChange = { categoriaExpander = !categoriaExpander },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = categoriaSeleccionada.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpander) },
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = categoriaExpander,
                    onDismissRequest = { categoriaExpander = false }
                ) {
                    ServiceCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                categoriaSeleccionada = category
                                categoriaExpander = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Descripción *")
            OutlinedTextField(
                value = descripcion,
                onValueChange = { if (it.length <= 500) { descripcion = it; viewModel.onDescriptionChanged(it) } },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Describe tu servicio...", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                isError = descriptionError != null,
                supportingText = { descriptionError?.let { Text(it.asString()) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Precio estimado (COP) *")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = precioMin,
                    onValueChange = { if (it.all { c -> c.isDigit() }) { precioMin = it; viewModel.onPricesChanged(it, precioMax) } },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Mínimo", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = precioMax,
                    onValueChange = { if (it.all { c -> c.isDigit() }) { precioMax = it; viewModel.onPricesChanged(precioMin, it) } },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Máximo", color = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            priceError?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Ubicación *")
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth().clickable { onSelectLocationClick() },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color.Gray) },
                trailingIcon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary) },
                placeholder = { Text("Selecciona en el mapa", color = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.LightGray,
                    disabledPlaceholderColor = Color.Gray
                )
            )
            Text(
                "Usar mi dirección actual", 
                color = MaterialTheme.colorScheme.primary, 
                fontSize = 12.sp, 
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { 
                        locationPermissionsLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel("Imágenes (Máx. 3) *")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uploadedImages) { index, image ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(if (image.isPrimary) 2.dp else 1.dp, if (image.isPrimary) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectPrimaryImage(index) }
                    ) {
                        AsyncImage(model = image.url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(onClick = { viewModel.removeImage(index) }, modifier = Modifier.size(24.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.4f), CircleShape)) {
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
                                    if (status == PackageManager.PERMISSION_GRANTED) galleryLauncher.launch("image/*")
                                    else galleryPermissionLauncher.launch(galleryPermission)
                                },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isUploading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                else Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
}
