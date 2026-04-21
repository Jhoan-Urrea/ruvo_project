package com.example.ruvo_app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.usecase.UpdateUserProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onCityChanged(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun onUpdateClicked(onSuccess: () -> Unit) {
        val state = _uiState.value
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (state.fullName.isBlank() || state.phone.isBlank() || state.city.isBlank()) {
            _uiState.update { it.copy(error = "Por favor, completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = updateUserProfileUseCase(uid, state.fullName, state.phone, state.city)
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al actualizar perfil") }
            }
        }
    }
}

data class EditProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val city: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)