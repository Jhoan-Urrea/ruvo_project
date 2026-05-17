package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    suspend fun saveServicePost(post: ServicePost): Result<Unit>
    fun getServicePosts(): Flow<List<ServicePost>>
    fun getServicePostsByAuthor(authorId: String): Flow<List<ServicePost>>
    fun getAllServicePosts(): Flow<List<ServicePost>>
    suspend fun updatePostStatus(postId: String, newStatus: PostStatus): Result<Unit>
    suspend fun toggleLike(postId: String, userId: String): Result<Unit>
    
    // Filtros geográficos dinámicos
    fun getAvailableCountries(): Flow<List<String>>
    fun getAvailableRegions(): Flow<List<String>>
    fun getAvailableCities(): Flow<List<String>>
}
