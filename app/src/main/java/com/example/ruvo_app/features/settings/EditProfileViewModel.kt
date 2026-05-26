package com.example.ruvo_app.features.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.repository.ImageStorageService
import com.example.ruvo_app.domain.repository.UserRepository
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
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val userRepository: UserRepository,
    private val storageService: ImageStorageService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserProfile(uid).collect { result ->
                result.onSuccess { user ->
                    _uiState.update { it.copy(
                        fullName = user.fullName,
                        phone = user.phone,
                        city = user.location?.address ?: "",
                        profilePictureUrl = user.profilePictureUrl
                    ) }
                }
            }
        }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, error = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, error = null) }
    }

    fun onCityChanged(city: String) {
        _uiState.update { it.copy(city = city, error = null) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true, error = null) }
            val result = storageService.uploadImage(uri)
            result.onSuccess { resource ->
                _uiState.update { it.copy(profilePictureUrl = resource.url, isUploadingImage = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = "error_upload_failed", isUploadingImage = false) }
            }
        }
    }

    fun onUpdateClicked(onSuccess: () -> Unit) {
        val state = _uiState.value
        val uid = auth.currentUser?.uid ?: return

        if (state.fullName.isBlank() || state.phone.isBlank() || state.city.isBlank()) {
            _uiState.update { it.copy(error = "error_required_fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = updateUserProfileUseCase(
                uid = uid, 
                fullName = state.fullName, 
                phone = state.phone, 
                city = state.city,
                profilePictureUrl = state.profilePictureUrl
            )
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "error_unknown") }
            }
        }
    }
}

data class EditProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val city: String = "",
    val profilePictureUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
