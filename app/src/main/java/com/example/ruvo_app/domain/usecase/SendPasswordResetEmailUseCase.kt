package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordResetEmail(email)
    }
}
