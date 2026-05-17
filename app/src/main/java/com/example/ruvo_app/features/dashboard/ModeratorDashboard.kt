package com.example.ruvo_app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.core.component.AdminPostCard
import com.example.ruvo_app.core.component.AdminUserCard
import com.example.ruvo_app.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboard(
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ModeratorViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var selectedAdminTab by remember { mutableIntStateOf(0) }
    
    val allPosts by viewModel.allPosts.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val stats by viewModel.stats.collectAsState()

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
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
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
                0 -> AdminStatisticsScreen(stats)
                1 -> UsersManagement(
                    query = searchQuery, 
                    filter = selectedFilter, 
                    onQueryChange = { searchQuery = it }, 
                    onFilterChange = { selectedFilter = it },
                    users = allUsers,
                    onChangeRole = { userId, newRole -> viewModel.changeUserRole(userId, newRole) }
                )
                2 -> PostsManagement(
                    query = searchQuery, 
                    filter = selectedFilter, 
                    onQueryChange = { searchQuery = it }, 
                    onFilterChange = { selectedFilter = it },
                    posts = allPosts,
                    onApprove = { viewModel.approvePost(it) },
                    onReject = { postId, reason -> viewModel.rejectPost(postId, reason) }
                )
            }
        }
    }
}

@Composable
fun AdminStatisticsScreen(stats: AdminStats) {
    Column {
        Text(text = "Reportes Estadísticos", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Métricas generales de la plataforma", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatisticMetricRow("Usuarios Registrados", "${stats.totalUsers}", Color(0xFF4285F4))
            StatisticMetricRow("Servicios Totales", "${stats.totalServices}", Color(0xFF34A853))
            StatisticMetricRow("Publicaciones Pendientes", "${stats.pendingPosts}", Color(0xFFFBBC05))
            StatisticMetricRow("Reportes Activos", "${stats.activeReports}", Color(0xFFEA4335))
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
fun UsersManagement(
    query: String, 
    filter: String, 
    onQueryChange: (String) -> Unit, 
    onFilterChange: (String) -> Unit,
    users: List<User>,
    onChangeRole: (String, UserRole) -> Unit
) {
    var showRoleConfirm by remember { mutableStateOf<User?>(null) }
    
    if (showRoleConfirm != null) {
        val newRole = if (showRoleConfirm!!.role == UserRole.USER) UserRole.MODERATOR else UserRole.USER
        AlertDialog(
            onDismissRequest = { showRoleConfirm = null },
            confirmButton = {
                Button(onClick = { 
                    onChangeRole(showRoleConfirm!!.id, newRole)
                    showRoleConfirm = null 
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showRoleConfirm = null }) { Text("Cancelar") }
            },
            title = { Text("Cambiar Rol") },
            text = { Text("¿Deseas cambiar el rol de ${showRoleConfirm!!.fullName} a ${newRole.name}?") }
        )
    }

    Column {
        Text(text = "Gestión de Usuarios", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        AdminSearchBar(query, onQueryChange, "Buscar usuarios...")
        Spacer(modifier = Modifier.height(8.dp))
        AdminFilterRow(filter, onFilterChange, listOf("Todos", "Activos", "Bloqueados"))
        
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val filtered = users.filter { 
                it.fullName.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
            items(filtered) { user -> 
                AdminUserCard(user = user, onChangeRole = { showRoleConfirm = user }) 
            }
        }
    }
}

@Composable
fun PostsManagement(
    query: String, 
    filter: String, 
    onQueryChange: (String) -> Unit, 
    onFilterChange: (String) -> Unit,
    posts: List<ServicePost>,
    onApprove: (String) -> Unit,
    onReject: (String, String) -> Unit
) {
    var postToReject by remember { mutableStateOf<String?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    if (postToReject != null) {
        AlertDialog(
            onDismissRequest = { postToReject = null },
            title = { Text("Motivo de Rechazo") },
            text = {
                Column {
                    Text("Por favor, explica brevemente por qué se rechaza esta publicación:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: Imágenes inapropiadas o descripción incompleta") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(postToReject!!, rejectionReason)
                        postToReject = null
                        rejectionReason = ""
                    },
                    enabled = rejectionReason.isNotBlank()
                ) { Text("Confirmar Rechazo") }
            },
            dismissButton = {
                TextButton(onClick = { postToReject = null }) { Text("Cancelar") }
            }
        )
    }

    Column {
        Text(text = "Moderación", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
        AdminSearchBar(query, onQueryChange, "Buscar publicaciones...")
        Spacer(modifier = Modifier.height(8.dp))
        AdminFilterRow(filter, onFilterChange, listOf("Todos", "Pendientes", "Aprobados"))
        
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val filtered = posts.filter { 
                val matchesQuery = it.title.contains(query, ignoreCase = true)
                val matchesFilter = when(filter) {
                    "Pendientes" -> it.status == PostStatus.PENDIENTE
                    "Aprobados" -> it.status == PostStatus.VERIFICADO
                    else -> true
                }
                matchesQuery && matchesFilter
            }
            items(filtered) { post -> 
                AdminPostCard(
                    post = post, 
                    authorName = "Proveedor", 
                    date = "Reciente",
                    onApprove = { onApprove(post.id) },
                    onReject = { postToReject = post.id }
                ) 
            }
        }
    }
}

@Composable
fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun AdminFilterRow(selected: String, onSelect: (String) -> Unit, filters: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        items(filters) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun AdminBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        val items = listOf(
            Triple("Estadísticas", Icons.Default.BarChart, 0),
            Triple("Usuarios", Icons.Default.Group, 1),
            Triple("Moderación", Icons.Default.Rule, 2)
        )
        items.forEach { (name, icon, index) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = name) },
                label = { Text(name, fontSize = 10.sp) }
            )
        }
    }
}
