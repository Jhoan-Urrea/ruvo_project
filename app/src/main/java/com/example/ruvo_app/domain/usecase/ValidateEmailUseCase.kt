package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.util.AuthError

class ValidateEmailUseCase {
    operator fun invoke(email: String): Result<Unit> {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return if (email.isNotBlank() && email.matches(emailRegex)) {
            Result.success(Unit)
        } else {
            Result.failure(AuthError.InvalidEmail)
        }
    }
}