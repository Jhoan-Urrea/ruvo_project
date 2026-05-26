package com.example.ruvo_app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.data.local.UserPreferencesManager
import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.model.User
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.usecase.*
import com.example.ruvo_app.domain.util.AuthError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _newNotificationEvent = MutableSharedFlow<Notification>()
    val newNotificationEvent = _newNotificationEvent.asSharedFlow()

    private var lastNotificationTimestamp: Long = System.currentTimeMillis()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        getCurrentUserUseCase()
            .onEach { user ->
                if (user != null) {
                    loadFullProfile(user.id)
                    observeNotifications(user.id)
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeNotifications(uid: String) {
        notificationRepository.getNotifications(uid)
            .onEach { notifications ->
                val latest = notifications.firstOrNull()
                if (latest != null && latest.timestamp > lastNotificationTimestamp && !latest.isRead) {
                    _newNotificationEvent.emit(latest)
                    lastNotificationTimestamp = latest.timestamp
                } else if (latest != null) {
                    lastNotificationTimestamp = latest.timestamp
                }
            }
            .launchIn(viewModelScope)
    }

    fun onLoginEvent(email: String, pass: String) {
        val emailValidation = validateEmailUseCase(email)
        val passValidation = validatePasswordUseCase(pass)

        if (emailValidation.isFailure) {
            val error = emailValidation.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Email inválido")
            return
        }
        if (passValidation.isFailure) {
            val error = passValidation.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Contraseña inválida")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, pass)
                .onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al iniciar sesión")
                }
        }
    }

    fun onRegisterEvent(user: User, pass: String) {
        val emailVal = validateEmailUseCase(user.email)
        val passVal = validatePasswordUseCase(pass)
        val userVal = validateUsernameUseCase(user.username)

        if (emailVal.isFailure) {
            val error = emailVal.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Email inválido")
            return
        }
        if (passVal.isFailure) {
            val error = passVal.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Contraseña inválida")
            return
        }
        if (userVal.isFailure) {
            val error = userVal.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Username inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(user, pass)
                .onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al registrar")
                }
        }
    }

    fun onResetPasswordEvent(email: String) {
        val emailVal = validateEmailUseCase(email)
        if (emailVal.isFailure) {
            val error = emailVal.exceptionOrNull() as? AuthError
            _uiState.value = AuthUiState.Error(error?.message ?: "Email inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            resetPasswordUseCase(email)
                .onSuccess {
                    _uiState.value = AuthUiState.Unauthenticated
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al enviar correo")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            signOutUseCase()
            userPreferencesManager.clearUserPreferences()
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    private fun loadFullProfile(uid: String) {
        viewModelScope.launch {
            userRepository.getUserProfile(uid).collect { result ->
                result.onSuccess { user ->
                    userPreferencesManager.saveUserPreferences(user.id, user.email, user.role.name)
                    _uiState.value = AuthUiState.Authenticated(user)
                }.onFailure { e ->
                    _uiState.value = AuthUiState.Error(e.message ?: "Error al cargar perfil")
                }
            }
        }
    }
}

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Unauthenticated : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
