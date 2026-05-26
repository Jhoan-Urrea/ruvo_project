package com.example.ruvo_app.features.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.core.component.LocationDropdown
import com.example.ruvo_app.core.component.PriceFilterSheet
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.features.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onServiceClick: (Screen.DetalleServicio) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("País") }
    var selectedRegion by remember { mutableStateOf("Región") }
    var selectedCity by remember { mutableStateOf("Ciudad") }
    var maxPriceFilter by remember { mutableStateOf<Float?>(null) }
    var showPriceFilter by remember { mutableStateOf(false) }

    val services by viewModel.services.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val cities by viewModel.cities.collectAsState()

    if (showPriceFilter) {
        PriceFilterSheet(
            currentMaxPrice = maxPriceFilter,
            onDismiss = { showPriceFilter = false },
            onPriceSelected = { 
                maxPriceFilter = it
                showPriceFilter = false
            }
        )
    }

    val filteredServices = remember(searchQuery, selectedCountry, selectedRegion, selectedCity, maxPriceFilter, services) {
        services.filter { service ->
            val matchesQuery = if (searchQuery.isBlank()) true 
                              else service.title.contains(searchQuery, ignoreCase = true) ||
                                   service.description.contains(searchQuery, ignoreCase = true)
            
            val matchesCountry = if (selectedCountry == "País") true 
                                else service.country.equals(selectedCountry, ignoreCase = true)
            
            val matchesRegion = if (selectedRegion == "Región") true 
                               else service.region.equals(selectedRegion, ignoreCase = true)
            
            val matchesCity = if (selectedCity == "Ciudad") true 
                             else service.city.equals(selectedCity, ignoreCase = true)
            
            val matchesPrice = if (maxPriceFilter == null) true
                              else service.minPrice <= maxPriceFilter!!
            
            matchesQuery && matchesCountry && matchesRegion && matchesCity && matchesPrice
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Buscador",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(27.dp)),
                        placeholder = { Text("¿Qué servicio buscas?", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        trailingIcon = { 
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                            } else {
                                IconButton(onClick = { showPriceFilter = true }) {
                                    Icon(Icons.Default.FilterList, null, tint = if (maxPriceFilter != null) MaterialTheme.colorScheme.primary else Color.Black)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F3F4),
                            unfocusedContainerColor = Color(0xFFF1F3F4),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LocationDropdown(
                            selectedOption = selectedCountry,
                            options = countries,
                            onOptionSelected = { selectedCountry = it; selectedRegion = "Región"; selectedCity = "Ciudad" },
                            modifier = Modifier.weight(1f),
                            placeholder = "País"
                        )
                        LocationDropdown(
                            selectedOption = selectedRegion,
                            options = if (selectedCountry == "País") regions else regions.filter { r -> services.any { it.country == selectedCountry && it.region == r } || r == "Región" },
                            onOptionSelected = { selectedRegion = it; selectedCity = "Ciudad" },
                            modifier = Modifier.weight(1f),
                            placeholder = "Región"
                        )
                        LocationDropdown(
                            selectedOption = selectedCity,
                            options = if (selectedRegion == "Región") cities else cities.filter { c -> services.any { it.region == selectedRegion && it.city == c } || c == "Ciudad" },
                            onOptionSelected = { selectedCity = it },
                            modifier = Modifier.weight(1f),
                            placeholder = "Ciudad"
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredServices) { post ->
                    SearchServiceCard(
                        post = post,
                        onClick = {
                            onServiceClick(
                                Screen.DetalleServicio(
                                    id = post.id, title = post.title, description = post.description,
                                    category = post.category.name, location = post.addressText,
                                    priceRange = "$ ${post.minPrice.toInt()} - $ ${post.maxPrice.toInt()}",
                                    providerId = post.authorId, providerName = post.authorName,
                                    providerSpecialty = "Especialista", providerImageRes = R.drawable.isotipo,
                                    rating = post.rating, reviewsCount = post.reviewsCount,
                                    imageRes = R.drawable.card_service,
                                    imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                                )
                            )
                        }
                    )
                }
                
                if (filteredServices.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No se encontraron servicios", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchServiceCard(post: ServicePost, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(100.dp).fillMaxHeight().padding(8.dp).clip(RoundedCornerShape(8.dp))) {
                val img = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                if (img != null) {
                    AsyncImage(
                        model = img, 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.card_service), 
                        contentDescription = null, 
                        modifier = Modifier.fillMaxSize(), 
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (post.status == PostStatus.VERIFICADO) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color(0xFF10B981), CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.title, 
                        style = MaterialTheme.typography.titleSmall, 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (post.status == PostStatus.VERIFICADO) {
                        Icon(
                            Icons.Default.Verified, 
                            contentDescription = "Verificado", 
                            tint = Color(0xFF10B981), 
                            modifier = Modifier.size(16.dp).padding(start = 4.dp)
                        )
                    }
                }
                Text(text = post.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Text(text = post.city, fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(text = "$ ${post.minPrice.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
