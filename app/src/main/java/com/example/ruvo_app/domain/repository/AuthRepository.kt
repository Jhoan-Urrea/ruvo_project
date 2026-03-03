package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(user: User, password: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}