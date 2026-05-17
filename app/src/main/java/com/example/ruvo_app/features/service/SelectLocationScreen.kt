package com.example.ruvo_app.features.service

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ruvo_app.BuildConfig
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLocationScreen(
    onLocationSelected: (Double, Double, String, String, String, String, String) -> Unit, // lat, lng, full, country, region, city, exact
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    var selectedLatLng by remember { mutableStateOf(LatLng(4.6243, -74.0636)) } 
    var addressText by remember { mutableStateOf("Localizando...") }
    
    // Detailed location data
    var currentCountry by remember { mutableStateOf("") }
    var currentRegion by remember { mutableStateOf("") }
    var currentCity by remember { mutableStateOf("") }
    var currentExact by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    fun processAddress(address: Address) {
        addressText = address.getAddressLine(0) ?: "Ubicación seleccionada"
        currentCountry = address.countryName ?: ""
        currentRegion = address.adminArea ?: ""
        currentCity = address.locality ?: address.subAdminArea ?: ""
        currentExact = address.thoroughfare ?: address.featureName ?: ""
    }

    fun updateAddressFromCoords(latLng: LatLng) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) processAddress(addresses[0])
                    }
                    override fun onError(errorMessage: String?) {
                        Log.e("Geocoder", "Error: $errorMessage")
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    processAddress(addresses[0])
                }
            }
        } catch (e: Exception) {
            addressText = "Punto marcado"
        }
    }

    fun moveToCurrentLocation(isInitial: Boolean = false) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val current = LatLng(it.latitude, it.longitude)
                    selectedLatLng = current
                    if (isInitial) {
                        mapInstance?.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0))
                    } else {
                        mapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0))
                    }
                    updateAddressFromCoords(current)
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) moveToCurrentLocation()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveToCurrentLocation(isInitial = true)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun searchAddress(query: String) {
        if (query.isBlank()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val found = addresses[0]
                            val newLatLng = LatLng(found.latitude, found.longitude)
                            selectedLatLng = newLatLng
                            processAddress(found)
                            mapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0))
                        }
                    }
                    override fun onError(errorMessage: String?) {
                        Log.e("MapSearch", "Error: $errorMessage")
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val found = results[0]
                    val newLatLng = LatLng(found.latitude, found.longitude)
                    selectedLatLng = newLatLng
                    processAddress(found)
                    mapInstance?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15.0))
                }
            }
            focusManager.clearFocus()
        } catch (e: Exception) {
            Log.e("MapSearch", "Error searching: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { Text("Ubicación del Servicio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Busca una dirección o ciudad...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { searchAddress(searchQuery) }),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = { moveToCurrentLocation() },
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
                }
                ExtendedFloatingActionButton(
                    onClick = { 
                        onLocationSelected(
                            selectedLatLng.latitude, 
                            selectedLatLng.longitude, 
                            addressText,
                            currentCountry,
                            currentRegion,
                            currentCity,
                            currentExact
                        ) 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Check, null) },
                    text = { Text("Confirmar punto") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx ->
                    MapLibre.getInstance(ctx)
                    MapView(ctx).apply {
                        getMapAsync { map ->
                            mapInstance = map
                            map.setStyle(Style.Builder().fromUri("https://tiles.stadiamaps.com/styles/alidade_smooth.json?api_key=${BuildConfig.STADIA_API_KEY}"))
                            map.cameraPosition = CameraPosition.Builder()
                                .target(selectedLatLng)
                                .zoom(14.0)
                                .build()
                            
                            map.addOnCameraIdleListener {
                                val center = map.cameraPosition.target
                                if (center != null) {
                                    selectedLatLng = center
                                    updateAddressFromCoords(center)
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Marcador visual central (Pin de RUVO)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF0047FF),
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp)
            )

            // Badge de información de dirección
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
                    .offset(y = (-80).dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF0047FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = addressText, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.SemiBold, 
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
