package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.Review
import com.example.ruvo_app.domain.repository.ReviewRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ReviewRepository {

    override suspend fun addReview(review: Review): Result<Unit> {
        return try {
            val docRef = firestore.collection("reviews").document()
            val reviewWithId = review.copy(id = docRef.id)
            
            firestore.runTransaction { transaction ->
                // 1. Save the review
                transaction.set(docRef, reviewWithId)
                
                // 2. Update provider reputation
                val providerRef = firestore.collection("users").document(review.providerId)
                val providerSnap = transaction.get(providerRef)
                
                val currentRating = providerSnap.getDouble("rating") ?: 0.0
                val totalReviews = providerSnap.getLong("totalReviews") ?: 0L
                
                val newTotalReviews = totalReviews + 1
                val newRating = ((currentRating * totalReviews) + review.rating) / newTotalReviews
                
                transaction.update(providerRef, "rating", newRating)
                transaction.update(providerRef, "totalReviews", newTotalReviews)
                
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReviewsForProvider(providerId: String): Flow<List<Review>> = callbackFlow {
        val subscription = firestore.collection("reviews")
            .whereEqualTo("providerId", providerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { it.toObject(Review::class.java) } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { subscription.remove() }
    }

    override fun getReviewsForService(serviceId: String): Flow<List<Review>> = callbackFlow {
        val subscription = firestore.collection("reviews")
            .whereEqualTo("serviceId", serviceId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { it.toObject(Review::class.java) } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { subscription.remove() }
    }
}
