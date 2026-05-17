package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(uid: String): Flow<Result<User>>
    suspend fun saveUserProfile(user: User): Result<Unit>
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit>
    fun getAllUsers(): Flow<List<User>> // Added for admin management
}