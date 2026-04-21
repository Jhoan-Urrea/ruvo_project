package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.Service
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {
    suspend fun saveService(service: Service): Result<Unit>
    fun getServices(): Flow<List<Service>>
}
