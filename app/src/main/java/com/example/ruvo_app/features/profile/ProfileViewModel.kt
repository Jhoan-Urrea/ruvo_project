package com.example.ruvo_app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.usecase.GetServicePostsByAuthorUseCase
import com.example.ruvo_app.domain.usecase.SignOutUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getServicePostsByAuthorUseCase: GetServicePostsByAuthorUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userFlow = userRepository.getUserProfile(currentUser.uid)
            val postsFlow = getServicePostsByAuthorUseCase(currentUser.uid)

            userFlow.combine(postsFlow) { userResult, posts ->
                userResult.fold(
                    onSuccess = { user ->
                        ProfileUiState.Success(user, posts)
                    },
                    onFailure = { e ->
                        ProfileUiState.Error(e.message ?: "Error al cargar perfil")
                    }
                )
            }.onEach { state ->
                _uiState.value = state
            }.launchIn(viewModelScope)
        } else {
            _uiState.value = ProfileUiState.Error("No hay sesión activa")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase.invoke()
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User, val services: List<ServicePost>) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}