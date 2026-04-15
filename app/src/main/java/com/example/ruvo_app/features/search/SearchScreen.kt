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
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.features.dashboard.LocationDropdown
import com.example.ruvo_app.features.dashboard.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onServiceClick: (Screen.DetalleServicio) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Lista completa de servicios (puedes expandirla con más ejemplos)
    val allServices = listOf(
        Service(
            id = "1",
            title = "Tutor de conceptos matemáticos",
            description = "Licenciado en Matemáticas de la Universidad La Liberta del Mundo Imaginario",
            category = "Educación",
            location = "Armenia, Quindío",
            priceRange = "$ 30.000 - $60.000 x Hora",
            userName = "Juan Hurtado",
            userSpecialty = "Licenciado",
            imageRes = R.drawable.tutor
        ),
        Service(
            id = "2",
            title = "Tutor de conceptos económicos",
            description = "Experto en microeconomía y macroeconomía con 5 años de experiencia.",
            category = "Educación",
            location = "Armenia, Quindío",
            priceRange = "$ 30.000 - $60.000 x Hora",
            userName = "Juan Hurtado",
            userSpecialty = "Licenciado",
            imageRes = R.drawable.tutor
        ),
        Service(
            id = "3",
            title = "Mudanzas Express",
            description = "Servicio de mudanzas nacionales e internacionales con el mejor cuidado.",
            category = "Hogar",
            location = "Bogotá, Cundinamarca",
            priceRange = "$ 150.000 - $500.000",
            userName = "Camilo Ruiz",
            userSpecialty = "Transportador",
            imageRes = R.drawable.mudanza
        ),
        Service(
            id = "4",
            title = "Plomería Profesional",
            description = "Arreglo de tuberías, grifería y filtraciones. Servicio garantizado 24/7.",
            category = "Hogar",
            location = "Medellín, Antioquia",
            priceRange = "$ 50.000 - $120.000",
            userName = "Andrés López",
            userSpecialty = "Plomero certificado",
            imageRes = R.drawable.plomero
        ),
        Service(
            id = "5",
            title = "Paseador de Perros",
            description = "Paseos recreativos para todas las razas. Amante de los animales.",
            category = "Mascotas",
            location = "Pereira, Risaralda",
            priceRange = "$ 15.000 - $25.000 x Hora",
            userName = "Diego Marín",
            userSpecialty = "Entrenador Canino",
            imageRes = R.drawable.paseador_perros
        )
    )

    // Lógica de filtrado en tiempo real
    val filteredServices = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allServices
        } else {
            allServices.filter { service ->
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

                LocationDropdown("Todos", Modifier.weight(1f))
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
            items(filteredServices) { service ->
                SearchServiceCard(
                    service = service,
                    onClick = {
                        onServiceClick(
                            Screen.DetalleServicio(
                                id = service.id,
                                title = service.title,
                                description = service.description,
                                category = service.category,
                                location = service.location,
                                priceRange = service.priceRange,
                                userName = service.userName,
                                userSpecialty = service.userSpecialty,
                                imageRes = service.imageRes
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
fun SearchServiceCard(service: Service, onClick: () -> Unit) {
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
                Image(
                    painter = painterResource(id = service.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Badge de Categoría dinámico
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                   Icon(
                       imageVector = when(service.category) {
                           "Educación" -> Icons.Default.Book
                           "Hogar" -> Icons.Default.Home
                           "Mascotas" -> Icons.Default.Pets
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
                        text = service.title,
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
                    text = service.description,
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
                            text = service.location,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = service.priceRange.split("x").first().trim(),
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
