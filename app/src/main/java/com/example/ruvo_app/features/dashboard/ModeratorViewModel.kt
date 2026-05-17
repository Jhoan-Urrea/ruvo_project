package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.usecase.ChangeUserRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _allPosts = MutableStateFlow<List<ServicePost>>(emptyList())
    val allPosts: StateFlow<List<ServicePost>> = _allPosts.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combinamos los flujos de posts y usuarios para calcular estadísticas reales en tiempo real
            combine(
                serviceRepository.getAllServicePosts(),
                userRepository.getAllUsers()
            ) { posts, users ->
                _allPosts.value = posts
                _allUsers.value = users
                
                AdminStats(
                    totalUsers = users.size,
                    totalServices = posts.size,
                    pendingPosts = posts.count { it.status == PostStatus.PENDIENTE },
                    activeReports = 0 
                )
            }.collect { updatedStats ->
                _stats.value = updatedStats
            }
        }
    }

    fun approvePost(postId: String) {
        val post = _allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            val result = serviceRepository.updatePostStatus(postId, PostStatus.VERIFICADO)
            if (result.isSuccess) {
                // Notificar al autor sobre la aprobación
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = post.authorId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "¡Felicidades! Tu servicio '${post.title}' ha sido aprobado y ya es visible para todos."
                    )
                )
            }
        }
    }

    fun rejectPost(postId: String) {
        val post = _allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            val result = serviceRepository.updatePostStatus(postId, PostStatus.RECHAZADO)
            if (result.isSuccess) {
                // Notificar al autor sobre el rechazo
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = post.authorId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "Tu servicio '${post.title}' ha sido rechazado tras la revisión."
                    )
                )
            }
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            changeUserRoleUseCase(userId, newRole)
        }
    }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val totalServices: Int = 0,
    val pendingPosts: Int = 0,
    val activeReports: Int = 0
)
