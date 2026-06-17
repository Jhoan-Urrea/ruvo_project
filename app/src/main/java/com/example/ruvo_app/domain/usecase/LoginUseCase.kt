package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repository.login(email, password)
    }
}