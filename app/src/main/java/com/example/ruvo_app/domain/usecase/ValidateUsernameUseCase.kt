package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.util.AuthError

class ValidateUsernameUseCase {
    operator fun invoke(username: String): Result<Unit> {
        return if (username.isNotBlank() && username.length >= 3) {
            Result.success(Unit)
        } else {
            Result.failure(AuthError.InvalidUsername)
        }
    }
}