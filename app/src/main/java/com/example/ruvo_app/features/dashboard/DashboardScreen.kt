package com.example.ruvo_app.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.features.notifications.NotificationsScreen
import com.example.ruvo_app.features.profile.ProfileScreen
import com.example.ruvo_app.features.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAddPostClick: () -> Unit = {},
    onAdminDetailedClick: () -> Unit = {},
    onServiceClick: (Screen.DetalleServicio) -> Unit = {},
    isAdmin: Boolean = false
) {
    var selectedCategory by remember { mutableStateOf("Todo") }
    // Normalizamos los índices: 0: Inicio, 1: Buscar, 2: Notificaciones, 3: Perfil
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            if (selectedTab == 0) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.isotipo),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RUVO",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (isAdmin) {
                            IconButton(onClick = onAdminDetailedClick) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "Admin Panel",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Mensajería */ }) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Mensajes",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                BottomNavigationBar(selectedTab) { 
                    selectedTab = it
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    HomeContent(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it },
                        onAddClick = onAddPostClick,
                        onServiceClick = onServiceClick
                    )
                }
                1 -> SearchScreen(onServiceClick = onServiceClick)
                2 -> NotificationsScreen()
                3 -> ProfileScreen(onSettingsClick = onSettingsClick)
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Pantalla en construcción")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    selectedCategory: String, 
    onCategorySelect: (String) -> Unit,
    onAddClick: () -> Unit,
    onServiceClick: (Screen.DetalleServicio) -> Unit
) {
    val services = listOf(
        Service(
            id = "1",
            title = "Tutor de conceptos matemáticos",
            description = "Licenciado en Matematicas de la Universidad La Liberta del Mundo Imaginario",
            category = "Educación",
            location = "Armenia, Quindío",
            priceRange = "$ 30.000 - $60.000 x Hora",
            userName = "Juan Hurtado",
            userSpecialty = "Licenciado",
            imageRes = R.drawable.tutor
        ),
        Service(
            id = "2",
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
            id = "3",
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
            id = "4",
            title = "Limpieza de Hogar",
            description = "Limpieza profunda de casas y apartamentos. Personal confiable y honesto.",
            category = "Hogar",
            location = "Cali, Valle",
            priceRange = "$ 60.000 - $90.000",
            userName = "Marta Gómez",
            userSpecialty = "Auxiliar de servicios",
            imageRes = R.drawable.limpieza
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
        ),
        Service(
            id = "6",
            title = "Servicio Técnico PC",
            description = "Reparación de hardware y software, mantenimiento preventivo y correctivo.",
            category = "Educación", // O según convenga
            location = "Manizales, Caldas",
            priceRange = "$ 40.000 - $100.000",
            userName = "Kevin Castro",
            userSpecialty = "Técnico en sistemas",
            imageRes = R.drawable.servicio_tecnico
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
        ) {
            // Sección Superior Blanca
            Column(modifier = Modifier.background(Color.White)) {
                // Categorías
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val categories = listOf(
                    CategoryItem("Todo", null),
                    CategoryItem("Hogar", "🏠"),
                    CategoryItem("Educación", "📚"),
                    CategoryItem("Mascotas", "🐾")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(category.name) },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    if (category.emoji != null) {
                                        Text(text = category.emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = category.name,
                                        fontSize = 11.sp, // Fuente un poco más pequeña para que quepa todo
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0047FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Gray
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Filtros de ubicación
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))

                    LocationDropdown("Todos", Modifier.weight(1.2f))
                    LocationDropdown("Región", Modifier.weight(1f))
                    LocationDropdown("Ciudad", Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Listado de Tarjetas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filteredServices = if (selectedCategory == "Todo") {
                    services
                } else {
                    services.filter { it.category == selectedCategory }
                }

                items(filteredServices) { service ->
                    ServiceCard(
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

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        // FAB en Inicio
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = Color(0xFF0047FF),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear", modifier = Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(service: Service, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Imagen con Badges
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
            ) {
                Image(
                    painter = painterResource(id = service.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Badge Categoría
                Surface(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD3E0FF).copy(alpha = 0.9f)
                ) {
                    Text(
                        text = service.category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF0047FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Badge Destacado
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0047FF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Destacado",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Contenido
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE0F7E9)
                    ) {
                        Text(
                            text = "Verificado",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF2ECC71),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = service.location, fontSize = 11.sp, color = Color.Gray)
                    }

                    Text(
                        text = service.priceRange,
                        fontSize = 12.sp,
                        color = Color(0xFF0047FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(12.dp))

                // Footer (Usuario y Stats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.isotipo), // Usando isotipo como placeholder de avatar
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = service.userName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = service.userSpecialty, fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(text = " ${service.commentsCount}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(text = " ${service.likesCount}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

data class Service(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val location: String,
    val priceRange: String,
    val userName: String,
    val userSpecialty: String,
    val imageRes: Int,
    val isVerified: Boolean = true,
    val isFeatured: Boolean = true,
    val commentsCount: Int = 5,
    val likesCount: Int = 20
)

@Composable
fun LocationDropdown(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5),
        border = BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, fontSize = 12.sp, color = Color.Gray)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp) 
    ) {
        val items = listOf(
            NavigationItem("Inicio", Icons.Default.Home, Icons.Outlined.Home),
            NavigationItem("Buscar", Icons.Default.Search, Icons.Outlined.Search),
            NavigationItem("Notificaciones", Icons.Default.Notifications, Icons.Outlined.Notifications),
            NavigationItem("Perfil", Icons.Default.Person, Icons.Outlined.Person)
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.name
                    )
                },
                label = { 
                    Text(
                        text = item.name, 
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class CategoryItem(val name: String, val emoji: String?)
data class NavigationItem(val name: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    Ruvo_appTheme {
        DashboardScreen()
    }
}