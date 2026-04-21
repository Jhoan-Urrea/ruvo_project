package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.Service
import com.example.ruvo_app.domain.model.ServiceStatus
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ServiceRepository {

    override suspend fun saveService(service: Service): Result<Unit> {
        return try {
            val docRef = if (service.id.isEmpty()) {
                firestore.collection("services").document()
            } else {
                firestore.collection("services").document(service.id)
            }
            
            val serviceWithId = if (service.id.isEmpty()) service.copy(id = docRef.id) else service
            
            docRef.set(ServiceDto.fromDomain(serviceWithId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getServices(): Flow<List<Service>> = callbackFlow {
        val subscription = firestore.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val services = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServiceDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                
                trySend(services)
            }
        awaitClose { subscription.remove() }
    }
}

data class ServiceDto(
    val title: String = "",
    val authorName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "",
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val imageUrl: String? = null,
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val status: String = "ACTIVE"
) {
    fun toDomain(id: String) = Service(
        id = id,
        title = title,
        authorName = authorName,
        description = description,
        price = price,
        priceUnit = priceUnit,
        rating = rating,
        reviewsCount = reviewsCount,
        imageUrl = imageUrl,
        category = category,
        location = location,
        date = date,
        status = try { ServiceStatus.valueOf(status) } catch(e: Exception) { ServiceStatus.ACTIVE }
    )

    companion object {
        fun fromDomain(service: Service) = ServiceDto.fromDomainInternal(service)
        
        private fun fromDomainInternal(service: Service) = ServiceDto(
            title = service.title,
            authorName = service.authorName,
            description = service.description,
            price = service.price,
            priceUnit = service.priceUnit,
            rating = service.rating,
            reviewsCount = service.reviewsCount,
            imageUrl = service.imageUrl,
            category = service.category,
            location = service.location,
            date = service.date,
            status = service.status.name
        )
    }
}
