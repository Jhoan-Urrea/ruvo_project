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
import androidx.compose.material.icons.outlined.MoreVert
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
import com.example.ruvo_app.core.component.ServicePostCard
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.ServiceCategory
import com.example.ruvo_app.domain.model.GeoPoint
import com.example.ruvo_app.features.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onServiceClick: (Screen.DetalleServicio) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val services by viewModel.services.collectAsState()
    val cities by viewModel.cities.collectAsState()

    val filteredServices = remember(searchQuery, services) {
        if (searchQuery.isBlank()) {
            services
        } else {
            services.filter { service ->
                service.title.contains(searchQuery, ignoreCase = true) ||
                service.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header (Buscador y Filtros)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Buscador",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de Búsqueda con funcionalidad de filtrado
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                placeholder = { Text("¿Qué servicio buscas?", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
                trailingIcon = { 
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    } else {
                        Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Color.Black)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F3F4),
                    unfocusedContainerColor = Color(0xFFF1F3F4),
                    disabledContainerColor = Color(0xFFF1F3F4),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de Ubicación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                LocationDropdown(cities.firstOrNull() ?: "Todos", Modifier.weight(1f))
                LocationDropdown("Region", Modifier.weight(1f))
                LocationDropdown("Ciudad", Modifier.weight(1f))
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        // Lista de Servicios Filtrados
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
                                id = post.id,
                                title = post.title,
                                description = post.description,
                                category = post.category.name,
                                location = post.addressText,
                                priceRange = "$ ${post.minPrice.toInt()} - $ ${post.maxPrice.toInt()}",
                                providerId = post.authorId,
                                providerName = "Proveedor",
                                providerSpecialty = "Especialista",
                                providerImageRes = R.drawable.isotipo,
                                rating = 4.8f,
                                reviewsCount = 12,
                                imageRes = R.drawable.card_service,
                                imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                            )
                        )
                    }
                )
            }
            
            // Estado cuando no hay resultados
            if (filteredServices.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron servicios",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen a la Izquierda
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val primaryImage = post.images.find { it.isPrimary } ?: post.images.firstOrNull()
                if (primaryImage != null) {
                    AsyncImage(
                        model = primaryImage.url,
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
                
                // Badge de Categoría dinámico
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                   Icon(
                       imageVector = when(post.category) {
                           ServiceCategory.EDUCACION -> Icons.Default.Book
                           ServiceCategory.HOGAR -> Icons.Default.Home
                           ServiceCategory.MASCOTAS -> Icons.Default.Pets
                           else -> Icons.Default.Work
                       },
                       contentDescription = null,
                       modifier = Modifier.size(14.dp),
                       tint = Color(0xFF6200EE)
                   )
                }
            }

            // Contenido a la Derecha
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verificado",
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                    )
                }

                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = post.city.ifEmpty { post.addressText },
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "$ ${post.minPrice.toInt()}",
                        fontSize = 10.sp,
                        color = Color(0xFF0047FF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
