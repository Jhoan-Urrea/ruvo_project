package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceDetailUiState>(ServiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    fun loadService(postId: String) {
        viewModelScope.launch {
            // In a real app, we would fetch from repository
            // For now, we listen to the stream if available or fetch a single one
            serviceRepository.getServicePosts().collect { posts ->
                val post = posts.find { it.id == postId }
                if (post != null) {
                    _uiState.value = ServiceDetailUiState.Success(
                        post = post,
                        isOwner = post.authorId == currentUserId
                    )
                } else {
                    _uiState.value = ServiceDetailUiState.Error("Servicio no encontrado")
                }
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
    data class Success(val post: ServicePost, val isOwner: Boolean) : ServiceDetailUiState()
    data class Error(val message: String) : ServiceDetailUiState()
}
