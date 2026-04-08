package com.example.ruvo_app.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ruvo_app.R
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.features.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAddPostClick: () -> Unit = {},
    onAdminDetailedClick: () -> Unit = {},
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
                        onAddClick = onAddPostClick
                    )
                }
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
    onAddClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
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

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category.name
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category.name) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (category.emoji != null) {
                                    Text(text = category.emoji)
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(text = category.name)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.LightGray,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de ubicación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                
                LocationDropdown("Todos", Modifier.weight(1f))
                LocationDropdown("Región", Modifier.weight(1f))
                LocationDropdown("Ciudad", Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray, thickness = 0.5.dp)

            // Listado de Tarjetas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aquí se listarán las tarjetas", color = Color.Gray)
            }
        }

        // FAB en Inicio
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear")
        }
    }
}

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