package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    // operator fun invoke(...) = repository.login(...)
}