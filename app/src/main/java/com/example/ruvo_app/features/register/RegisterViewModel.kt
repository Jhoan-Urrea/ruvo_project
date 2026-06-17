package com.example.ruvo_app.features.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
import com.example.ruvo_app.domain.usecase.RegisterUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val gamificationService: GamificationService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, error = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, error = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onRegisterClicked(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Simple validation
        if (state.fullName.isBlank() || state.phone.isBlank() || 
            state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "error_required_fields") }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "error_password_too_short") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val user = User(
                id = "",
                fullName = state.fullName,
                phone = state.phone,
                email = state.email,
                username = state.fullName.replace(" ", "").lowercase() // Default username logic
            )
            
            val result = registerUseCase(user, state.password)
            
            _uiState.update { it.copy(isLoading = false) }
            
            result.onSuccess {
                // Award Welcome Achievement
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    gamificationService.checkAndAwardAchievement(currentUserId, Achievement.Bienvenido)
                }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "error_unknown") }
            }
        }
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
