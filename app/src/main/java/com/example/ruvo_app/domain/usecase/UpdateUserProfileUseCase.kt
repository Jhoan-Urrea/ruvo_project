package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.repository.UserRepository

class UpdateUserProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(
        uid: String, 
        fullName: String, 
        phone: String, 
        city: String,
        profilePictureUrl: String? = null
    ): Result<Unit> {
        val updates = mutableMapOf<String, Any>(
            "fullName" to fullName,
            "phone" to phone,
            "location.address" to city
        )
        profilePictureUrl?.let {
            updates["profilePictureUrl"] = it
        }
        return repository.updateUserProfile(uid, updates)
    }
}
