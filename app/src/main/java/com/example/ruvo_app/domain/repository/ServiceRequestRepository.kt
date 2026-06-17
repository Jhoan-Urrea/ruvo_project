package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.ServiceRequest
import kotlinx.coroutines.flow.Flow

interface ServiceRequestRepository {
    suspend fun createRequest(request: ServiceRequest): Result<Unit>
    fun getRequestsForCustomer(customerId: String): Flow<List<ServiceRequest>>
    fun getRequestsForProvider(providerId: String): Flow<List<ServiceRequest>>
    fun getAllRequests(): Flow<List<ServiceRequest>>
    suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit>
}
