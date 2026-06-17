package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.model.UserRole
import com.example.ruvo_app.domain.repository.UserRepository

class ChangeUserRoleUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(targetUid: String, newRole: UserRole): Result<Unit> {
        val updates = mapOf("role" to newRole.name)
        return repository.updateUserProfile(targetUid, updates)
    }
}