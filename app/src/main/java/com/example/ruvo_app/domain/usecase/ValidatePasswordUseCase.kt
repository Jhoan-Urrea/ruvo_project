package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.util.AuthError

class ValidatePasswordUseCase {
    operator fun invoke(password: String): Result<Unit> {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasNumber = password.any { it.isDigit() }
        
        return if (hasMinLength && hasUpperCase && hasNumber) {
            Result.success(Unit)
        } else {
            Result.failure(AuthError.InvalidPassword)
        }
    }
}