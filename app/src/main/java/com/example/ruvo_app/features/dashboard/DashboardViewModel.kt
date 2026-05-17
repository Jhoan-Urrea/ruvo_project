package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
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

    init {
        loadServices()
        loadFilters()
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
}
