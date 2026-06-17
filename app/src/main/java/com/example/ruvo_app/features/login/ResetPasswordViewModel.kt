package com.example.ruvo_app.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.usecase.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(
            newPassword = password,
            error = null,
            hasMinLength = password.length >= 8,
            hasUpperCase = password.any { it.isUpperCase() },
            hasLowerCase = password.any { it.isLowerCase() },
            hasNumberOrSymbol = password.any { !it.isLetter() }
        ) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun onResetPasswordClicked(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        if (state.newPassword.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.update { it.copy(error = "error_required_fields") }
            return
        }

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(error = "error_passwords_dont_match") }
            return
        }

        if (!state.hasMinLength || !state.hasUpperCase || !state.hasLowerCase || !state.hasNumberOrSymbol) {
            _uiState.update { it.copy(error = "error_password_requirements") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = changePasswordUseCase(state.newPassword)
            
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isPasswordReset = true) }
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(
                    isLoading = false, 
                    error = e.message ?: "Ocurrió un error al cambiar la contraseña"
                ) }
            }
        }
    }
}

data class ResetPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val hasMinLength: Boolean = false,
    val hasUpperCase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasNumberOrSymbol: Boolean = false,
    val isLoading: Boolean = false,
    val isPasswordReset: Boolean = false,
    val error: String? = null
)
