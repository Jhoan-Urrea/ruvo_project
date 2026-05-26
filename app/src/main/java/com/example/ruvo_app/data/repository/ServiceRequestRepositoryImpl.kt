package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.RequestStatus
import com.example.ruvo_app.domain.model.ServiceRequest
import com.example.ruvo_app.domain.repository.ServiceRequestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ServiceRequestRepository {

    override suspend fun createRequest(request: ServiceRequest): Result<Unit> {
        return try {
            val docRef = firestore.collection("service_requests").document()
            val requestWithId = request.copy(id = docRef.id)
            docRef.set(ServiceRequestDto.fromDomain(requestWithId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRequestsForCustomer(customerId: String): Flow<List<ServiceRequest>> = callbackFlow {
        val subscription = firestore.collection("service_requests")
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { it.toObject(ServiceRequestDto::class.java)?.toDomain(it.id) } ?: emptyList()
                trySend(requests)
            }
        awaitClose { subscription.remove() }
    }

    override fun getRequestsForProvider(providerId: String): Flow<List<ServiceRequest>> = callbackFlow {
        val subscription = firestore.collection("service_requests")
            .whereEqualTo("providerId", providerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { it.toObject(ServiceRequestDto::class.java)?.toDomain(it.id) } ?: emptyList()
                trySend(requests)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllRequests(): Flow<List<ServiceRequest>> = callbackFlow {
        val subscription = firestore.collection("service_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { it.toObject(ServiceRequestDto::class.java)?.toDomain(it.id) } ?: emptyList()
                trySend(requests)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("service_requests").document(requestId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ServiceRequestDto(
    val id: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val offeredPrice: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val urgency: String = "",
    val details: String = "",
    val status: String = "PENDIENTE",
    val createdAt: Long = 0L
) {
    fun toDomain(docId: String) = ServiceRequest(
        id = docId,
        serviceId = serviceId,
        serviceTitle = serviceTitle,
        customerId = customerId,
        customerName = customerName,
        providerId = providerId,
        providerName = providerName,
        offeredPrice = offeredPrice,
        date = date,
        time = time,
        location = location,
        urgency = urgency,
        details = details,
        status = try { RequestStatus.valueOf(status) } catch (e: Exception) { RequestStatus.PENDIENTE },
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: ServiceRequest) = ServiceRequestDto(
            id = domain.id,
            serviceId = domain.serviceId,
            serviceTitle = domain.serviceTitle,
            customerId = domain.customerId,
            customerName = domain.customerName,
            providerId = domain.providerId,
            providerName = domain.providerName,
            offeredPrice = domain.offeredPrice,
            date = domain.date,
            time = domain.time,
            location = domain.location,
            urgency = domain.urgency,
            details = domain.details,
            status = domain.status.name,
            createdAt = domain.createdAt
        )
    }
}
