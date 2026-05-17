package com.example.ruvo_app.features.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.NotificationType
import com.example.ruvo_app.domain.model.RequestStatus
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val repository: ServiceRequestRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val gamificationService: GamificationService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _solicitudesRecibidas = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val solicitudesRecibidas = _solicitudesRecibidas.asStateFlow()

    private val _solicitudesEnviadas = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val solicitudesEnviadas = _solicitudesEnviadas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val currentUserId = auth.currentUser?.uid ?: ""

    init {
        loadSolicitudes()
    }

    private fun loadSolicitudes() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar solicitudes como proveedor
            launch {
                repository.getRequestsForProvider(currentUserId).collect { requests ->
                    _solicitudesRecibidas.value = requests
                }
            }
            
            // Cargar solicitudes como cliente
            launch {
                repository.getRequestsForCustomer(currentUserId).collect { requests ->
                    _solicitudesEnviadas.value = requests
                    _isLoading.value = false
                }
            }
        }
    }

    fun responderSolicitud(request: ServiceRequest, nuevoEstado: RequestStatus) {
        viewModelScope.launch {
            val result = repository.updateRequestStatus(request.id, nuevoEstado.name)
            if (result.isSuccess) {
                val mensaje = when (nuevoEstado) {
                    RequestStatus.ACEPTADA -> "¡Tu solicitud para '${request.serviceTitle}' ha sido aceptada!"
                    RequestStatus.RECHAZADA -> "Lo sentimos, tu solicitud para '${request.serviceTitle}' ha sido rechazada."
                    RequestStatus.COMPLETADA -> "El proveedor ha marcado el trabajo '${request.serviceTitle}' como finalizado."
                    else -> "Tu solicitud ha cambiado de estado."
                }
                
                // GAMIFICACIÓN: Si se completa el trabajo
                if (nuevoEstado == RequestStatus.COMPLETADA) {
                    // 1. Logro para el proveedor (si es el primero)
                    gamificationService.checkAndAwardAchievement(request.providerId, Achievement.ManoDeObra)
                    
                    // 2. Puntos base por completar trabajo (+100 XP para ambos)
                    userRepository.addUserPoints(request.providerId, 100)
                    userRepository.addUserPoints(request.customerId, 100)
                    
                    // 3. Actualizar estadísticas del usuario (finishedPosts)
                    userRepository.updateUserProfile(request.providerId, mapOf("finishedPosts" to com.google.firebase.firestore.FieldValue.increment(1)))
                }

                notificationRepository.sendNotification(
                    Notification(
                        receiverId = request.customerId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = mensaje
                    )
                )
            }
        }
    }

    fun cancelarSolicitud(request: ServiceRequest) {
        viewModelScope.launch {
            repository.updateRequestStatus(request.id, RequestStatus.CANCELADA.name)
        }
    }
}
