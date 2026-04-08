package com.example.ruvo_app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.core.component.AdminPostCard
import com.example.ruvo_app.core.component.AdminServiceCard
import com.example.ruvo_app.core.component.AdminUserCard
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboard(
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedAdminTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Panel de Administrador", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver", fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                AdminBottomNavigation(selectedAdminTab) { tabIndex ->
                    selectedAdminTab = tabIndex
                    selectedFilter = "Todos"
                    searchQuery = ""
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            when (selectedAdminTab) {
                0 -> AdminStatisticsScreen()
                1 -> ServicesManagement(searchQuery, selectedFilter, { searchQuery = it }, { selectedFilter = it })
                2 -> UsersManagement(searchQuery, selectedFilter, { searchQuery = it }, { selectedFilter = it })
                3 -> PostsManagement(searchQuery, selectedFilter, { searchQuery = it }, { selectedFilter = it })
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Sección en desarrollo")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatisticsScreen() {
    Column {
        Text(text = "Reportes Estadísticos", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Métricas generales de la plataforma", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticMetricRow("Usuarios Registrados", "150", Color(0xFF4285F4))
            StatisticMetricRow("Servicios Totales", "45", Color(0xFF34A853))
            StatisticMetricRow("Publicaciones Realizadas", "230", Color(0xFFFBBC05))
            StatisticMetricRow("Reportes Activos", "12", Color(0xFFEA4335))
        }
    }
}

@Composable
fun StatisticMetricRow(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, fontWeight = FontWeight.Medium)
            }
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun ServicesManagement(query: String, filter: String, onQueryChange: (String) -> Unit, onFilterChange: (String) -> Unit) {
    val mockServices = listOf(
        Service(title = "Plomería residencial", authorName = "Juan Pérez", location = "Madrid", date = "2026-04-05", price = 50.0, status = ServiceStatus.ACTIVE, description = "", reviewsCount = 0, rating = 0f, category = ""),
        Service(title = "Clases de matemáticas", authorName = "Maria Garcia", location = "Barcelona", date = "2026-04-06", price = 30.0, priceUnit = "/hora", status = ServiceStatus.ACTIVE, description = "", reviewsCount = 0, rating = 0f, category = ""),
        Service(title = "Paseo de mascotas", authorName = "Carlos López", location = "Valencia", date = "2026-04-07", price = 15.0, status = ServiceStatus.PENDING, description = "", reviewsCount = 0, rating = 0f, category = ""),
        Service(title = "Reparación eléctrica", authorName = "Ana Martinez", location = "Sevilla", date = "2026-04-08", price = 40.0, status = ServiceStatus.ACTIVE, description = "", reviewsCount = 0, rating = 0f, category = "")
    )

    Column {
        Text(text = "Gestión de Servicios", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Consulta y modifica servicios publicados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        AdminSearchBar(query, onQueryChange, "Buscar servicios o proveedores...")
        Spacer(modifier = Modifier.height(16.dp))
        AdminFilterRow(filter, onFilterChange, listOf("Todos", "Activos", "Pendientes", "Inactivos"))
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(mockServices) { service -> AdminServiceCard(service = service) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun UsersManagement(query: String, filter: String, onQueryChange: (String) -> Unit, onFilterChange: (String) -> Unit) {
    val mockUsers = listOf(
        User(id = "1", fullName = "Juan Pérez", email = "juan.perez@email.com", username = "juanp", status = AccountStatus.ACTIVE, lastActive = "Hace 2 horas", reputation = Reputation(rating = 4.8f), stats = UserStats(activePosts = 12, reportsCount = 0)),
        User(id = "2", fullName = "María García", email = "maria.garcia@email.com", username = "mariag", status = AccountStatus.ACTIVE, lastActive = "Hace 1 día", reputation = Reputation(rating = 4.9f), stats = UserStats(activePosts = 8, reportsCount = 0)),
        User(id = "3", fullName = "Carlos López", email = "carlos.lopez@email.com", username = "carlosl", status = AccountStatus.WARNING, lastActive = "Hace 3 horas", reputation = Reputation(rating = 3.5f), stats = UserStats(activePosts = 5, reportsCount = 3))
    )

    Column {
        Text(text = "Gestión de Usuarios", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Administra y modera usuarios de la plataforma", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminSummaryCard("3", "Activos", Color(0xFF166534), Modifier.weight(1f))
            AdminSummaryCard("1", "Advertencias", Color(0xFF854D0E), Modifier.weight(1f))
            AdminSummaryCard("1", "Bloqueados", Color(0xFF991B1B), Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        AdminSearchBar(query, onQueryChange, "Buscar usuarios por nombre o email...")
        Spacer(modifier = Modifier.height(16.dp))
        AdminFilterRow(filter, onFilterChange, listOf("Todos", "Activos", "Advertencias", "Bloqueados"))
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(mockUsers) { user -> AdminUserCard(user = user) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PostsManagement(query: String, filter: String, onQueryChange: (String) -> Unit, onFilterChange: (String) -> Unit) {
    val mockPosts = listOf(
        ServicePost(
            title = "Instalación de aire acondicionado",
            description = "Servicio profesional de instalación y mantenimiento de equipos de climatización para hogares y oficinas.",
            authorId = "user1",
            category = ServiceCategory.HOGAR,
            status = PostStatus.PENDIENTE,
            minPrice = 50.0, maxPrice = 100.0, addressText = "Madrid", coverageRadius = 10.0, coordinates = GeoPoint(0.0, 0.0)
        ),
        ServicePost(
            title = "¡Nuevo servicio de jardinería disponible!",
            description = "Ofrezco servicios de jardinería y mantenimiento de áreas verdes. Diseño de jardines personalizados.",
            authorId = "user2",
            category = ServiceCategory.HOGAR,
            status = PostStatus.VERIFICADO,
            minPrice = 20.0, maxPrice = 50.0, addressText = "Barcelona", coverageRadius = 5.0, coordinates = GeoPoint(0.0, 0.0)
        ),
        ServicePost(
            title = "Clases de guitarra para principiantes",
            description = "Clases personalizadas de guitarra acústica y eléctrica. Incluye material de apoyo.",
            authorId = "user3",
            category = ServiceCategory.EDUCACION,
            status = PostStatus.PENDIENTE,
            minPrice = 15.0, maxPrice = 30.0, addressText = "Valencia", coverageRadius = 0.0, coordinates = GeoPoint(0.0, 0.0)
        )
    )

    Column {
        Text(text = "Gestión de Publicaciones", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Modera y aprueba publicaciones, servicios y reportes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdminSummaryCard("7", "Total", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            AdminSummaryCard("3", "Pendientes", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            AdminSummaryCard("2", "Aprobados", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            AdminSummaryCard("1", "Reportados", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        AdminSearchBar(query, onQueryChange, "Buscar publicaciones o autores...")
        Spacer(modifier = Modifier.height(16.dp))
        AdminFilterRow(filter, onFilterChange, listOf("Todos", "Pendientes", "Aprobados", "Reportados", "Rechazados"))
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(mockPosts) { post -> 
                AdminPostCard(
                    post = post, 
                    authorName = if(post.authorId == "user1") "Pedro Sanchez" else "Laura Fernandez", 
                    date = "2026-04-07"
                ) 
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AdminSummaryCard(count: String, label: String, contentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = count, color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@Composable
fun AdminFilterRow(selected: String, onSelect: (String) -> Unit, filters: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0047FF),
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == filter, borderColor = Color.LightGray),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun AdminBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White, 
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        val adminItems = listOf(
            NavigationItem("Estadísticas", Icons.Default.BarChart, Icons.Default.BarChart),
            NavigationItem("Servicios", Icons.Default.Description, Icons.Default.Description),
            NavigationItem("Usuarios", Icons.Default.Group, Icons.Default.Group),
            NavigationItem("Publicaciones", Icons.Default.Settings, Icons.Default.Settings)
        )

        adminItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.selectedIcon,
                        contentDescription = item.name,
                        modifier = Modifier.size(28.dp)
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

@Preview(showBackground = true)
@Composable
fun ModeratorDashboardPreview() {
    Ruvo_appTheme { ModeratorDashboard() }
}
