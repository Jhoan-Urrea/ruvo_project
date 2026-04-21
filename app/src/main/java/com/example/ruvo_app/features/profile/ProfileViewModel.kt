package com.example.ruvo_app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.usecase.SignOutUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userRepository.getUserProfile(currentUser.uid)
                .onEach { result ->
                    result.onSuccess { user ->
                        _uiState.value = ProfileUiState.Success(user)
                    }.onFailure { e ->
                        _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
                    }
                }
                .launchIn(viewModelScope)
        } else {
            _uiState.value = ProfileUiState.Error("No hay sesión activa")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase.invoke()
            // The AuthViewModel in AppNavGraph should detect the state change and redirect
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}