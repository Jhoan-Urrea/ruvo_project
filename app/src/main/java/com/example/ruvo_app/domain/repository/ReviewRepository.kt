package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    suspend fun addReview(review: Review): Result<Unit>
    fun getReviewsForProvider(providerId: String): Flow<List<Review>>
    fun getReviewsForService(serviceId: String): Flow<List<Review>>
}
