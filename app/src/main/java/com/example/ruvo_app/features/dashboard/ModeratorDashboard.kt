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
import androidx.compose.material.icons.filled.Report
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
import com.example.ruvo_app.core.component.AdminReportCard
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
    val allReports by viewModel.allReports.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                "Moderación y Confianza", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                    )
                    if (isAnalyzing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        },
        bottomBar = {
            AdminBottomNavigation(selectedAdminTab) { tabIndex ->
                selectedAdminTab = tabIndex
                selectedFilter = "Todos"
                searchQuery = ""
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
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
                    onReject = { postId, reason -> viewModel.rejectPost(postId, reason) },
                    onReanalyze = { viewModel.reanalyzePost(it) },
                    isAnalyzingGlobal = isAnalyzing
                )
                3 -> ReportsManagement(
                    query = searchQuery,
                    filter = selectedFilter,
                    onQueryChange = { searchQuery = it },
                    onFilterChange = { selectedFilter = it },
                    reports = allReports,
                    posts = allPosts,
                    onResolve = { reportId, status, note -> viewModel.resolveReport(reportId, status, note) }
                )
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White, 
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val items = listOf(
            Triple("Dashboard", Icons.Default.BarChart, 0),
            Triple("Usuarios", Icons.Default.Group, 1),
            Triple("Contenido", Icons.Default.Rule, 2),
            Triple("Alertas", Icons.Default.Report, 3)
        )
        items.forEach { (name, icon, index) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = name) },
                label = { Text(name, fontSize = 9.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun AdminStatisticsScreen(stats: AdminStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Resumen de Moderación", 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Control de calidad y seguridad de la plataforma", 
                style = MaterialTheme.typography.bodySmall, 
                color = Color.Gray
            )
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatisticMetricRow("Usuarios Registrados", "${stats.totalUsers}", Color(0xFF4285F4))
                StatisticMetricRow("Publicaciones Totales", "${stats.totalServices}", Color(0xFF34A853))
                StatisticMetricRow("Pedidos Generados", "${stats.totalRequests}", Color(0xFF673AB7))
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
                Text("Moderación IA Gemini", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                
                StatisticMetricRow("Post de Alto Riesgo", "${stats.highRiskPosts}", Color(0xFFEF4444))
                StatisticMetricRow("Pendientes Manuales", "${stats.pendingPosts}", Color(0xFFFBBF24))
                StatisticMetricRow("Reportes de Usuario", "${stats.activeReports}", Color(0xFFEA4335))
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
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
    onReject: (String, String) -> Unit,
    onReanalyze: (String) -> Unit,
    isAnalyzingGlobal: Boolean
) {
    var postToReject by remember { mutableStateOf<String?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    if (postToReject != null) {
        AlertDialog(
            onDismissRequest = { postToReject = null },
            title = { Text("Rechazar Publicación") },
            text = {
                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Explica el motivo (ej: Contenido falso)") },
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(postToReject!!, rejectionReason)
                        postToReject = null
                        rejectionReason = ""
                    },
                    enabled = rejectionReason.isNotBlank()
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { postToReject = null }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AdminSearchBar(query, onQueryChange, "Buscar servicios...")
            AdminFilterRow(filter, onFilterChange, listOf("Todos", "Riesgo Alto", "Pendientes", "Aprobados"))
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val filtered = posts.filter { 
                val matchesQuery = it.title.contains(query, ignoreCase = true)
                val matchesFilter = when(filter) {
                    "Riesgo Alto" -> it.status == PostStatus.REVISION_MANUAL_PRIORITARIA
                    "Pendientes" -> it.status == PostStatus.PENDIENTE || it.status == PostStatus.REVISION_MANUAL
                    "Aprobados" -> it.status == PostStatus.VERIFICADO
                    else -> true
                }
                matchesQuery && matchesFilter
            }
            items(filtered) { post -> 
                AdminPostCard(
                    post = post, 
                    authorName = post.authorName,
                    date = "Reciente",
                    onApprove = { onApprove(post.id) },
                    onReject = { postToReject = post.id },
                    onReanalyze = { onReanalyze(post.id) },
                    isAnalyzing = isAnalyzingGlobal
                ) 
            }
        }
    }
}

@Composable
fun ReportsManagement(
    query: String,
    filter: String,
    onQueryChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    reports: List<Report>,
    posts: List<ServicePost>, 
    onResolve: (String, ReportStatus, String?) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AdminSearchBar(query, onQueryChange, "Buscar reportes...")
            AdminFilterRow(filter, onFilterChange, listOf("Todos", "Pendientes", "Resueltos"))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            val filtered = reports.filter {
                val matchesQuery = it.reason.contains(query, ignoreCase = true)
                val matchesFilter = when(filter) {
                    "Pendientes" -> it.status == ReportStatus.PENDIENTE
                    "Resueltos" -> it.status == ReportStatus.RESUELTO
                    else -> true
                }
                matchesQuery && matchesFilter
            }
            items(filtered) { report ->
                val serviceContext = if (report.type == ReportType.SERVICIO) {
                    posts.find { it.id == report.reportedId }
                } else null

                AdminReportCard(
                    report = report,
                    reportedItemAnalysis = serviceContext?.trustAnalysis,
                    reportedItemTrustScore = serviceContext?.trustScore,
                    reportedItemTrustDetails = serviceContext?.trustDetails ?: emptyMap(),
                    onResolve = { status, note -> onResolve(report.id, status, note) }
                )
            }
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
            text = { Text("¿Cambiar el rol de ${showRoleConfirm!!.fullName} a ${newRole.name}?") }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AdminSearchBar(query, onQueryChange, "Buscar por nombre o correo...")
            AdminFilterRow(filter, onFilterChange, listOf("Todos", "Activos", "Bloqueados"))
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
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
                Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White, 
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
fun AdminFilterRow(selected: String, onSelect: (String) -> Unit, filters: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp), 
        modifier = Modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter, fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
