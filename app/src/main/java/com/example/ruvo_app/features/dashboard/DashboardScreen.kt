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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ruvo_app.R
import com.example.ruvo_app.core.navigation.Screen
import com.example.ruvo_app.core.theme.Ruvo_appTheme
import com.example.ruvo_app.features.notifications.NotificationsScreen
import com.example.ruvo_app.features.profile.ProfileScreen
import com.example.ruvo_app.features.search.SearchScreen
import com.example.ruvo_app.core.component.ServicePostCard
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.core.component.LocationDropdown
import com.example.ruvo_app.core.component.DashboardShimmer
import java.text.NumberFormat
import java.util.Locale

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
    viewModel: DashboardViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf("Todo") }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val services by viewModel.services.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val regions by viewModel.regions.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val maxPriceFilter by viewModel.maxPriceFilter.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialSuccessMessage) {
        initialSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
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
                        onServiceClick = onServiceClick,
                        services = services,
                        countries = countries,
                        regions = regions,
                        cities = cities,
                        isLoading = isLoading,
                        maxPriceFilter = maxPriceFilter,
                        onPriceFilterChange = { viewModel.setMaxPriceFilter(it) }
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
                                providerName = "Proveedor",
                                providerSpecialty = "Especialista",
                                providerImageRes = R.drawable.isotipo,
                                rating = 4.5f,
                                reviewsCount = 10,
                                imageRes = R.drawable.card_service,
                                imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
                            )
                        )
                    }
                )
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Pantalla en construcción")
                    }
                }
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
    onPriceFilterChange: (Float?) -> Unit
) {
    var selectedCountry by remember { mutableStateOf("País") }
    var countryExpanded by remember { mutableStateOf(false) }

    var selectedRegion by remember { mutableStateOf("Región") }
    var regionExpanded by remember { mutableStateOf(false) }

    var selectedCity by remember { mutableStateOf("Ciudad") }
    var cityExpanded by remember { mutableStateOf(false) }

    var showPriceFilter by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    if (showPriceFilter) {
        ModalBottomSheet(
            onDismissRequest = { showPriceFilter = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PriceFilterContent(
                currentMaxPrice = maxPriceFilter,
                onPriceSelected = { 
                    onPriceFilterChange(it)
                    showPriceFilter = false
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))

                    // Country Dropdown
                    ExposedDropdownMenuBox(
                        expanded = countryExpanded,
                        onExpandedChange = { countryExpanded = !countryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedCountry,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(fontSize = 10.sp)
                        )
                        ExposedDropdownMenu(
                            expanded = countryExpanded,
                            onDismissRequest = { countryExpanded = false }
                        ) {
                            countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country, fontSize = 12.sp) },
                                    onClick = {
                                        selectedCountry = country
                                        countryExpanded = false
                                        selectedRegion = "Región"
                                        selectedCity = "Ciudad"
                                    }
                                )
                            }
                        }
                    }

                    // Region Dropdown
                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = !regionExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(fontSize = 10.sp)
                        )
                        ExposedDropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            regions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region, fontSize = 12.sp) },
                                    onClick = {
                                        selectedRegion = region
                                        regionExpanded = false
                                        selectedCity = "Ciudad"
                                    }
                                )
                            }
                        }
                    }

                    // City Dropdown
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = Color(0xFFF5F5F5),
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(fontSize = 10.sp)
                        )
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city, fontSize = 12.sp) },
                                    onClick = {
                                        selectedCity = city
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { showPriceFilter = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (maxPriceFilter != null) Color(0xFFE8EFFF) else Color(0xFFF5F5F5),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtro de Precio",
                            tint = if (maxPriceFilter != null) Color(0xFF0047FF) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                DashboardShimmer()
            } else {
                // Listado de Tarjetas Reales con Filtrado Compuesto
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val filteredServices = services.filter { post ->
                        val matchesCategory = if (selectedCategory == "Todo") true 
                                             else post.category.name.equals(selectedCategory, ignoreCase = true)
                        
                        val matchesCountry = if (selectedCountry == "País") true
                                             else post.country.equals(selectedCountry, ignoreCase = true)

                        val matchesRegion = if (selectedRegion == "Región") true
                                            else post.region.equals(selectedRegion, ignoreCase = true)
                        
                        val matchesCity = if (selectedCity == "Ciudad") true 
                                          else post.city.equals(selectedCity, ignoreCase = true)

                        val matchesPrice = if (maxPriceFilter == null) true
                                          else post.minPrice <= maxPriceFilter
                        
                        matchesCategory && matchesCountry && matchesRegion && matchesCity && matchesPrice
                    }

                    if (filteredServices.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No hay servicios disponibles con estos filtros", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredServices) { post ->
                            ServicePostCard(
                                post = post,
                                authorName = "Proveedor",
                                authorRole = "Verificado",
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
                                            rating = 4.5f,
                                            reviewsCount = 10,
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
            containerColor = Color(0xFF0047FF),
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

@Composable
fun PriceFilterContent(
    currentMaxPrice: Float?,
    onPriceSelected: (Float?) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(currentMaxPrice ?: 1000000f) }
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Filtrar por presupuesto",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mostrar servicios desde el precio mínimo hasta:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = format.format(sliderPosition),
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF0047FF),
            fontWeight = FontWeight.ExtraBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 10000f..2000000f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF0047FF),
                activeTrackColor = Color(0xFF0047FF),
                inactiveTrackColor = Color(0xFFE8EFFF)
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$ 10k", fontSize = 12.sp, color = Color.Gray)
            Text(text = "$ 2M+", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onPriceSelected(sliderPosition) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047FF))
        ) {
            Text("Aplicar Filtro", fontWeight = FontWeight.Bold)
        }
        
        TextButton(
            onClick = { onPriceSelected(null) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Limpiar Filtro", color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
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
