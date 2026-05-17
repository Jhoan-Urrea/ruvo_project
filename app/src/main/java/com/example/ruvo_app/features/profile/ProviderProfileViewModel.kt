package com.example.ruvo_app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.usecase.GetServicePostsByAuthorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getServicePostsByAuthorUseCase: GetServicePostsByAuthorUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderProfileUiState>(ProviderProfileUiState.Loading)
    val uiState: StateFlow<ProviderProfileUiState> = _uiState.asStateFlow()

    fun loadProviderProfile(providerId: String) {
        viewModelScope.launch {
            val userFlow = userRepository.getUserProfile(providerId)
            val postsFlow = getServicePostsByAuthorUseCase(providerId)

            userFlow.combine(postsFlow) { userResult, posts ->
                userResult.fold(
                    onSuccess = { user ->
                        ProviderProfileUiState.Success(user, posts)
                    },
                    onFailure = { e ->
                        ProviderProfileUiState.Error(e.message ?: "Error al cargar perfil del proveedor")
                    }
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed class ProviderProfileUiState {
    object Loading : ProviderProfileUiState()
    data class Success(val user: User, val services: List<ServicePost>) : ProviderProfileUiState()
    data class Error(val message: String) : ProviderProfileUiState()
}
