package com.example.ruvo_app.features.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServiceRequest
import com.example.ruvo_app.domain.repository.ServiceRequestRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SolicitarServicioViewModel @Inject constructor(
    private val repository: ServiceRequestRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SolicitarUiState>(SolicitarUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun enviarSolicitud(
        serviceId: String,
        serviceTitle: String,
        providerId: String,
        providerName: String,
        date: String,
        time: String,
        location: String,
        urgency: String,
        details: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = SolicitarUiState.Loading
            
            // Obtenemos el nombre del cliente actual
            val userProfileResult = userRepository.getUserProfile(userId).first()
            val customerName = userProfileResult.getOrNull()?.fullName ?: "Usuario"

            val request = ServiceRequest(
                serviceId = serviceId,
                serviceTitle = serviceTitle,
                customerId = userId,
                customerName = customerName,
                providerId = providerId,
                providerName = providerName,
                date = date,
                time = time,
                location = location,
                urgency = urgency,
                details = details
            )

            val result = repository.createRequest(request)
            if (result.isSuccess) {
                _uiState.value = SolicitarUiState.Success
            } else {
                _uiState.value = SolicitarUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
}

sealed class SolicitarUiState {
    object Idle : SolicitarUiState()
    object Loading : SolicitarUiState()
    object Success : SolicitarUiState()
    data class Error(val message: String) : SolicitarUiState()
}
