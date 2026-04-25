package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _allPosts = MutableStateFlow<List<ServicePost>>(emptyList())
    val allPosts: StateFlow<List<ServicePost>> = _allPosts.asStateFlow()

    init {
        loadAllPosts()
    }

    private fun loadAllPosts() {
        viewModelScope.launch {
            serviceRepository.getAllServicePosts().collect { posts ->
                _allPosts.value = posts
            }
        }
    }

    fun approvePost(postId: String) {
        viewModelScope.launch {
            serviceRepository.updatePostStatus(postId, PostStatus.VERIFICADO)
        }
    }

    fun rejectPost(postId: String) {
        viewModelScope.launch {
            serviceRepository.updatePostStatus(postId, PostStatus.RECHAZADO)
        }
    }
}
