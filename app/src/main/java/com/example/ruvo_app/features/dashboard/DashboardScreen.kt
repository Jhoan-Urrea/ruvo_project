package com.example.ruvo_app.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.features.notifications.NotificationsScreen
import com.example.ruvo_app.features.profile.ProfileScreen
import com.example.ruvo_app.features.search.SearchScreen
import com.example.ruvo_app.core.component.ServicePostCard
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.core.component.LocationDropdown
import com.example.ruvo_app.core.component.DashboardShimmer
import com.example.ruvo_app.core.component.PriceFilterSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onAddPostClick: () -> Unit = {},
    onAdminDetailedClick: () -> Unit = {},
    onServiceClick: (Screen.DetalleServicio) -> Unit = {},
    onChatListClick: () -> Unit = {},
    onSolicitudesClick: () -> Unit = {},
    onMisTrabajosClick: () -> Unit = {},
    isAdmin: Boolean = false,
    initialSuccessMessage: String? = null,
    initialTab: Int = 0,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf("Todo") }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    
    val services by viewModel.services.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val maxPriceFilter by viewModel.maxPriceFilter.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialSuccessMessage) {
        initialSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val backgroundColor = when(selectedTab) {
        1 -> Color(0xFFF8F9FA)
        2 -> Color(0xFFF5F5F5)
        3 -> Color(0xFFF5F7FA)
        else -> Color(0xFFF8F8F8)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                painter = painterResource(id = R.drawable.logo_ruvo),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.dashboard_title),
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
                        IconButton(onClick = onChatListClick) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.dashboard_messages),
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                    windowInsets = WindowInsets.statusBars
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                unreadCount = unreadCount,
                onTabSelected = { selectedTab = it }
            )
        },
        contentWindowInsets = WindowInsets(0.dp) 
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
                        onServiceClick = onServiceClick,
                        services = services,
                        countries = countries,
                        regions = regions,
                        cities = cities,
                        isLoading = isLoading,
                        maxPriceFilter = maxPriceFilter,
                        onPriceFilterChange = { viewModel.setMaxPriceFilter(it) },
                        sortOrder = sortOrder,
                        onSortChange = { viewModel.setSortOrder(it) }
                    )
                }
                1 -> SearchScreen(onServiceClick = onServiceClick)
                2 -> NotificationsScreen()
                3 -> ProfileScreen(
                    onSettingsClick = onSettingsClick,
                    onSolicitudesClick = onSolicitudesClick,
                    onMisTrabajosClick = onMisTrabajosClick,
                    onServiceClick = { post ->
                        onServiceClick(
                            Screen.DetalleServicio(
                                id = post.id,
                                title = post.title,
                                description = post.description,
                                category = post.category.name,
                                location = post.addressText,
                                priceRange = "$ ${post.minPrice.toInt()} - $ ${post.maxPrice.toInt()}",
                                providerId = post.authorId,
                                providerName = post.authorName,
                                providerSpecialty = "Especialista",
                                providerImageRes = R.drawable.isotipo,
                                rating = post.rating,
                                reviewsCount = post.reviewsCount,
                                imageRes = R.drawable.card_service,
                                imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    selectedCategory: String, 
    onCategorySelect: (String) -> Unit,
    onAddClick: () -> Unit,
    onServiceClick: (Screen.DetalleServicio) -> Unit,
    services: List<ServicePost>,
    countries: List<String>,
    regions: List<String>,
    cities: List<String>,
    isLoading: Boolean,
    maxPriceFilter: Float?,
    onPriceFilterChange: (Float?) -> Unit,
    sortOrder: com.example.ruvo_app.features.dashboard.SortOrder,
    onSortChange: (com.example.ruvo_app.features.dashboard.SortOrder) -> Unit
) {
    var selectedCountry by remember { mutableStateOf("País") }
    var selectedRegion by remember { mutableStateOf("Región") }
    var selectedCity by remember { mutableStateOf("Ciudad") }
    var showPriceFilter by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    if (showPriceFilter) {
        PriceFilterSheet(
            currentMaxPrice = maxPriceFilter,
            onDismiss = { showPriceFilter = false },
            onPriceSelected = { 
                onPriceFilterChange(it)
                showPriceFilter = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.background(Color.White)) {
                Text(
                    text = stringResource(R.string.dashboard_categories),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val categoriesList = listOf(
                    CategoryItem("Todo", null),
                    CategoryItem("Hogar", "🏠"),
                    CategoryItem("Educación", "📚"),
                    CategoryItem("Mascotas", "🐾"),
                    CategoryItem("Tecnología", "💻"),
                    CategoryItem("Transporte", "🚗")
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categoriesList) { category ->
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
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Gray
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // FILTROS DE UBICACIÓN REFINADOS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    LocationDropdown(
                        selectedOption = selectedCountry,
                        options = countries,
                        onOptionSelected = { 
                            selectedCountry = it
                            selectedRegion = "Región"
                            selectedCity = "Ciudad"
                        },
                        modifier = Modifier.weight(1f),
                        label = "País",
                        placeholder = "Seleccionar"
                    )

                    LocationDropdown(
                        selectedOption = selectedRegion,
                        options = if (selectedCountry == "País") regions else regions.filter { region ->
                            services.any { it.country == selectedCountry && it.region == region } || region == "Región"
                        },
                        onOptionSelected = { 
                            selectedRegion = it
                            selectedCity = "Ciudad"
                        },
                        modifier = Modifier.weight(1f),
                        label = "Región",
                        placeholder = "Seleccionar"
                    )

                    LocationDropdown(
                        selectedOption = selectedCity,
                        options = if (selectedRegion == "Región") cities else cities.filter { city ->
                            services.any { it.region == selectedRegion && it.city == city } || city == "Ciudad"
                        },
                        onOptionSelected = { selectedCity = it },
                        modifier = Modifier.weight(1f),
                        label = "Ciudad",
                        placeholder = "Seleccionar"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showPriceFilter = true },
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (maxPriceFilter != null) Color(0xFFE8EFFF) else Color.Transparent,
                            contentColor = if (maxPriceFilter != null) MaterialTheme.colorScheme.primary else Color.Gray
                        ),
                        border = BorderStroke(1.dp, if (maxPriceFilter != null) MaterialTheme.colorScheme.primary else Color.LightGray),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (maxPriceFilter != null) "Hasta $${maxPriceFilter.toInt()}" else "Presupuesto",
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            border = BorderStroke(1.dp, Color.LightGray),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = sortOrder.label, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            com.example.ruvo_app.features.dashboard.SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(order.label) },
                                    onClick = {
                                        onSortChange(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                DashboardShimmer()
            } else {
                val filteredAndSortedServices = remember(
                    services, selectedCategory, selectedCountry, selectedRegion, selectedCity, maxPriceFilter, sortOrder
                ) {
                    services.filter { post ->
                        val matchesCategory = if (selectedCategory == "Todo") true 
                                             else post.category.name.equals(selectedCategory, ignoreCase = true)
                        
                        val matchesCountry = if (selectedCountry == "País") true
                                             else post.country.equals(selectedCountry, ignoreCase = true)

                        val matchesRegion = if (selectedRegion == "Región") true
                                            else post.region.equals(selectedRegion, ignoreCase = true)
                        
                        val matchesCity = if (selectedCity == "Ciudad") true 
                                          else post.city.equals(selectedCity, ignoreCase = true)

                        val matchesPrice = if (maxPriceFilter == null) true
                                          else post.minPrice <= maxPriceFilter!!
                        
                        matchesCategory && matchesCountry && matchesRegion && matchesCity && matchesPrice
                    }.let { filtered ->
                        when (sortOrder) {
                            com.example.ruvo_app.features.dashboard.SortOrder.PRICE_LOW_HIGH -> filtered.sortedBy { it.minPrice }
                            com.example.ruvo_app.features.dashboard.SortOrder.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.minPrice }
                            com.example.ruvo_app.features.dashboard.SortOrder.RATING_HIGH_LOW -> filtered.sortedByDescending { it.rating }
                            else -> filtered.sortedByDescending { it.createdAt }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filteredAndSortedServices.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay servicios disponibles", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredAndSortedServices) { post ->
                            ServicePostCard(
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
                                            providerName = post.authorName,
                                            providerSpecialty = "Especialista",
                                            providerImageRes = R.drawable.isotipo,
                                            rating = post.rating,
                                            reviewsCount = post.reviewsCount,
                                            imageRes = R.drawable.card_service,
                                            imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dashboard_create), modifier = Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(selectedTab: Int, unreadCount: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val items = listOf(
            NavigationItem(stringResource(R.string.nav_home), Icons.Default.Home, Icons.Outlined.Home),
            NavigationItem(stringResource(R.string.nav_search), Icons.Default.Search, Icons.Outlined.Search),
            NavigationItem(stringResource(R.string.nav_notifications), Icons.Default.Notifications, Icons.Outlined.Notifications),
            NavigationItem(stringResource(R.string.nav_profile), Icons.Default.Person, Icons.Outlined.Person)
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (index == 2 && unreadCount > 0) {
                                Badge {
                                    Text(text = if (unreadCount > 9) "9+" else unreadCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.name
                        )
                    }
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
