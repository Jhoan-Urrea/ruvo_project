package com.example.ruvo_app.domain.usecase

import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServicePostsByAuthorUseCase @Inject constructor(
    private val repository: ServiceRepository
) {
    operator fun invoke(authorId: String): Flow<List<ServicePost>> {
        return repository.getServicePostsByAuthor(authorId)
    }
}
