package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.AuthRepository

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordResetEmail(email)
    }
}