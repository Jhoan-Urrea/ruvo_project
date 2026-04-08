package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.AuthRepository

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() {
        repository.signOut()
    }
}