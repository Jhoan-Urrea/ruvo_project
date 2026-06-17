package com.example.ruvo_app.features.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.R
import com.example.ruvo_app.core.utils.UiText
import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.NotificationType
import com.example.ruvo_app.domain.model.ServiceRequest
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ServiceRequestRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
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
    private val notificationRepository: NotificationRepository,
    private val gamificationService: GamificationService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SolicitarUiState>(SolicitarUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun enviarSolicitud(
        serviceId: String,
        serviceTitle: String,
        providerId: String,
        providerName: String,
        offeredPrice: Double,
        date: String,
        time: String,
        location: String,
        urgency: String,
        details: String
    ) {
        val userId = auth.currentUser?.uid ?: return
        
        if (date.isBlank() || time.isBlank() || details.length < 5) {
            _uiState.value = SolicitarUiState.Error(UiText.StringResource(R.string.error_form_invalid))
            return
        }

        viewModelScope.launch {
            _uiState.value = SolicitarUiState.Loading
            
            val userProfileResult = userRepository.getUserProfile(userId).first()
            val customerName = userProfileResult.getOrNull()?.fullName ?: "Un cliente"

            val request = ServiceRequest(
                serviceId = serviceId,
                serviceTitle = serviceTitle,
                customerId = userId,
                customerName = customerName,
                providerId = providerId,
                providerName = providerName,
                offeredPrice = offeredPrice,
                date = date,
                time = time,
                location = location,
                urgency = urgency,
                details = details
            )

            val result = repository.createRequest(request)
            if (result.isSuccess) {
                // NOTIFICACIÓN DIRECTA AL PROVEEDOR
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = providerId,
                        type = NotificationType.NUEVA_SOLICITUD,
                        message = "$customerName ha solicitado tu servicio '$serviceTitle'."
                    )
                )
                
                gamificationService.checkAndAwardAchievement(userId, Achievement.Explorador)
                _uiState.value = SolicitarUiState.Success
            } else {
                _uiState.value = SolicitarUiState.Error(UiText.DynamicString("No se pudo enviar la solicitud"))
            }
        }
    }
    
    fun resetState() {
        _uiState.value = SolicitarUiState.Idle
    }
}

sealed class SolicitarUiState {
    object Idle : SolicitarUiState()
    object Loading : SolicitarUiState()
    object Success : SolicitarUiState()
    data class Error(val message: UiText) : SolicitarUiState()
}
