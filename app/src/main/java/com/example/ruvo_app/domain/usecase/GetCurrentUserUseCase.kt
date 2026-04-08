package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> {
        return repository.getCurrentUser()
    }
}