package com.example.ruvo_app.features.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.Comment
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.CommentRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val gamificationService: GamificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun loadService(postId: String) {
        viewModelScope.launch {
            // Cargar el post
            serviceRepository.getAllServicePosts().collect { posts ->
                val post = posts.find { it.id == postId }
                if (post != null) {
                    userRepository.getUserProfile(post.authorId).collect { userResult ->
                        userResult.onSuccess { author ->
                            _uiState.value = ServiceDetailUiState.Success(
                                post = post,
                                isOwner = post.authorId == currentUserId,
                                isLiked = post.likedBy.contains(currentUserId),
                                author = author
                            )
                        }.onFailure {
                            _uiState.value = ServiceDetailUiState.Error("Error al cargar info del proveedor")
                        }
                    }
                } else {
                    _uiState.value = ServiceDetailUiState.Error("Servicio no encontrado")
                }
            }
        }
        
        // Cargar comentarios
        viewModelScope.launch {
            commentRepository.getCommentsForPost(postId).collect { list ->
                _comments.value = list
            }
        }
    }

    fun toggleLike(postId: String) {
        val userId = currentUserId ?: return
        val currentState = _uiState.value as? ServiceDetailUiState.Success ?: return
        
        viewModelScope.launch {
            val wasLiked = currentState.isLiked
            val result = serviceRepository.toggleLike(postId, userId)
            
            if (result.isSuccess && !wasLiked) {
                // GAMIFICACIÓN
                userRepository.addUserPoints(currentState.post.authorId, 10)
                userRepository.addUserPoints(userId, 5)
                
                if (currentState.post.likedBy.size + 1 >= 10) {
                    gamificationService.checkAndAwardAchievement(currentState.post.authorId, Achievement.Popular)
                }
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val userId = currentUserId ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val comment = Comment(
                postId = postId,
                authorId = userId,
                text = text
            )
            val result = commentRepository.addComment(comment)
            if (result.isSuccess) {
                // GAMIFICACIÓN: Logro Crítico
                gamificationService.checkAndAwardAchievement(userId, Achievement.Critico)
                userRepository.addUserPoints(userId, 20)
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
                serviceRepository.saveServicePost(currentState.post.copy(images = updatedImages))
            }
        }
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
