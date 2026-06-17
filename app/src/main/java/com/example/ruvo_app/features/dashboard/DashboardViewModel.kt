package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _services = MutableStateFlow<List<ServicePost>>(emptyList())
    val services: StateFlow<List<ServicePost>> = _services.asStateFlow()

    private val _countries = MutableStateFlow<List<String>>(listOf("País"))
    val countries: StateFlow<List<String>> = _countries.asStateFlow()

    private val _regions = MutableStateFlow<List<String>>(listOf("Región"))
    val regions: StateFlow<List<String>> = _regions.asStateFlow()

    private val _cities = MutableStateFlow<List<String>>(listOf("Ciudad"))
    val cities: StateFlow<List<String>> = _cities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount = _unreadNotificationsCount.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow<Float?>(null)
    val maxPriceFilter = _maxPriceFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder = _sortOrder.asStateFlow()

    val maxPriceInServices = _services.map { list ->
        list.maxOfOrNull { it.maxPrice.toFloat() } ?: 1000000f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000000f)

    init {
        loadServices()
        loadFilters()
        observeNotifications()
    }

    private fun loadServices() {
        viewModelScope.launch {
            _isLoading.value = true
            serviceRepository.getServicePosts().collect { list ->
                _services.value = list
                _isLoading.value = false
            }
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            serviceRepository.getAvailableCountries().collect { list ->
                _countries.value = listOf("País") + list
            }
        }
        viewModelScope.launch {
            serviceRepository.getAvailableRegions().collect { list ->
                _regions.value = listOf("Región") + list
            }
        }
        viewModelScope.launch {
            serviceRepository.getAvailableCities().collect { list ->
                _cities.value = listOf("Ciudad") + list
            }
        }
    }

    private fun observeNotifications() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            notificationRepository.getNotifications(uid).collect { list ->
                _unreadNotificationsCount.value = list.count { !it.isRead }
            }
        }
    }

    fun setMaxPriceFilter(price: Float?) {
        _maxPriceFilter.value = price
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
}

enum class SortOrder(val label: String) {
    DEFAULT("Relevancia"),
    PRICE_LOW_HIGH("Precio: Menor a Mayor"),
    PRICE_HIGH_LOW("Precio: Mayor a Menor"),
    RATING_HIGH_LOW("Mejor Calificados")
}
