package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.PostStatus
import com.example.ruvo_app.domain.model.ServicePost
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    suspend fun saveServicePost(post: ServicePost): Result<String>
    fun getServicePosts(): Flow<List<ServicePost>>
    fun getServicePostsByAuthor(authorId: String): Flow<List<ServicePost>>
    fun getAllServicePosts(): Flow<List<ServicePost>>
    fun getServicePostById(postId: String): Flow<ServicePost?>
    suspend fun updatePostStatus(postId: String, newStatus: PostStatus): Result<Unit>
    suspend fun updateTrustAnalysis(
        postId: String, 
        score: Double, 
        textScore: Int,
        imageScore: Int,
        analysis: String,
        details: Map<String, String>,
        aiConfidence: Double,
        riskHighlights: List<String>,
        newStatus: PostStatus
    ): Result<Unit>
    suspend fun archiveService(postId: String): Result<Unit>
    suspend fun reactivateService(postId: String): Result<Unit>
    suspend fun rejectPost(postId: String, reason: String): Result<Unit>
    suspend fun toggleLike(postId: String, userId: String): Result<Unit>
    suspend fun setAiFeedback(postId: String, isUseful: Boolean): Result<Unit>
    
    fun getAvailableCountries(): Flow<List<String>>
    fun getAvailableRegions(): Flow<List<String>>
    fun getAvailableCities(): Flow<List<String>>
}
