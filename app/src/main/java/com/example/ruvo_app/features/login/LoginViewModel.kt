package com.example.ruvo_app.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.core.di.AuthUseCases
import com.example.ruvo_app.data.local.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Validación básica en el ViewModel (Requisito académico)
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "error_required_fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Llamada al Caso de Uso (Firebase Auth)
            val result = authUseCases.login(state.email, state.password)
            
            if (result.isSuccess) {
                // 2. Persistir sesión en DataStore (Requisito académico)
                userPreferencesManager.saveUserPreferences(
                    uid = "logged_in", 
                    email = state.email, 
                    role = "USER"
                )
                
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    ) 
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
