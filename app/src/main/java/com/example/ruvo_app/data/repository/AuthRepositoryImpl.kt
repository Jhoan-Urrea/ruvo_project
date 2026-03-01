package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        // TODO: Implement actual login logic (API call)
        return Result.success(Unit)
    }

    override suspend fun register(user: User, password: String): Result<Unit> {
        // TODO: Implement actual registration logic (API call)
        return Result.success(Unit)
    }
}