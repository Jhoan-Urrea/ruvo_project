package com.example.ruvo_app.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.*
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository,
    private val reportRepository: ReportRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews = _reviews.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var serviceJob: Job? = null

    fun loadService(postId: String) {
        serviceJob?.cancel()
        
        serviceJob = viewModelScope.launch {
            _uiState.value = ServiceDetailUiState.Loading
            
            serviceRepository.getServicePostById(postId)
                .flatMapLatest { post ->
                    if (post != null) {
                        userRepository.getUserProfile(post.authorId).map { userResult ->
                            userResult.fold(
                                onSuccess = { author ->
                                    ServiceDetailUiState.Success(
                                        post = post,
                                        isOwner = post.authorId == currentUserId,
                                        isLiked = post.likedBy.contains(currentUserId),
                                        author = author
                                    )
                                },
                                onFailure = {
                                    ServiceDetailUiState.Error("Error al cargar info del proveedor")
                                }
                            )
                        }
                    } else {
                        flowOf(ServiceDetailUiState.Error("Servicio no encontrado"))
                    }
                }
                .catch { e ->
                    emit(ServiceDetailUiState.Error("Error de conexión: ${e.message}"))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
        
        viewModelScope.launch {
            reviewRepository.getReviewsForService(postId)
                .catch { _reviews.value = emptyList() }
                .collect { list -> _reviews.value = list }
        }

        viewModelScope.launch {
            commentRepository.getCommentsForPost(postId)
                .catch { _comments.value = emptyList() }
                .collect { list -> _comments.value = list }
        }
    }

    fun toggleLike(postId: String) {
        val userId = currentUserId ?: return
        val currentState = _uiState.value as? ServiceDetailUiState.Success ?: return
        
        viewModelScope.launch {
            try {
                val wasLiked = currentState.isLiked
                val result = serviceRepository.toggleLike(postId, userId)
                
                if (result.isSuccess && !wasLiked) {
                    userRepository.addUserPoints(currentState.post.authorId, 10)
                    userRepository.addUserPoints(userId, 5)
                    
                    if (currentState.post.likedBy.size + 1 >= 10) {
                        gamificationService.checkAndAwardAchievement(currentState.post.authorId, Achievement.Popular)
                    }
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val userId = currentUserId ?: return
        if (text.isBlank()) return
        val currentState = _uiState.value as? ServiceDetailUiState.Success ?: return

        viewModelScope.launch {
            try {
                // Obtenemos el perfil del usuario actual para desnormalizar datos en el comentario
                val currentUserProfile = userRepository.getUserProfile(userId).first().getOrNull()
                
                val comment = Comment(
                    postId = postId,
                    authorId = userId,
                    authorName = currentUserProfile?.fullName ?: "Usuario",
                    authorProfilePictureUrl = currentUserProfile?.profilePictureUrl,
                    text = text
                )
                
                val result = commentRepository.addComment(comment)
                if (result.isSuccess) {
                    if (currentState.post.authorId != userId) {
                        notificationRepository.sendNotification(
                            Notification(
                                receiverId = currentState.post.authorId,
                                type = NotificationType.NUEVO_COMENTARIO,
                                message = "${currentUserProfile?.fullName ?: "Alguien"} comentó en tu servicio '${currentState.post.title}'"
                            )
                        )
                    }
                    gamificationService.checkAndAwardAchievement(userId, Achievement.Critico)
                    userRepository.addUserPoints(userId, 20)
                }
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }

    fun setPrimaryImage(postId: String, imagePublicId: String) {
        val currentState = _uiState.value
        if (currentState is ServiceDetailUiState.Success) {
            val updatedImages = currentState.post.images.map {
                it.copy(isPrimary = it.publicId == imagePublicId)
            }
            viewModelScope.launch {
                try {
                    serviceRepository.saveServicePost(currentState.post.copy(images = updatedImages))
                } catch (e: Exception) {}
            }
        }
    }

    fun reportService(reason: String, description: String) {
        val userId = currentUserId ?: return
        val currentState = _uiState.value as? ServiceDetailUiState.Success ?: return
        
        viewModelScope.launch {
            try {
                val report = Report(
                    reportedId = currentState.post.id,
                    reporterId = userId,
                    type = ReportType.SERVICIO,
                    reason = reason,
                    description = description
                )
                reportRepository.createReport(report)
            } catch (e: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        serviceJob?.cancel()
    }
}

sealed class ServiceDetailUiState {
    object Loading : ServiceDetailUiState()
    data class Success(
        val post: ServicePost, 
        val isOwner: Boolean, 
        val isLiked: Boolean,
        val author: User
    ) : ServiceDetailUiState()
    data class Error(val message: String) : ServiceDetailUiState()
}
