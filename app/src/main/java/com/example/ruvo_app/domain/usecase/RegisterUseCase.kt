package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User, password: String): Result<Unit> {
        return repository.register(user, password)
    }
}