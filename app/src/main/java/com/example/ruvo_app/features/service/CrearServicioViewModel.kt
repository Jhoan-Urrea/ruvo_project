package com.example.ruvo_app.features.service

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.ImageStorageService
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrearServicioViewModel @Inject constructor(
    private val storageService: ImageStorageService,
    private val serviceRepository: ServiceRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _uploadedImages = MutableStateFlow<List<ImageResource>>(emptyList())
    val uploadedImages = _uploadedImages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    // Reactive validation states
    private val _titleError = MutableStateFlow<String?>(null)
    val titleError = _titleError.asStateFlow()

    private val _priceError = MutableStateFlow<String?>(null)
    val priceError = _priceError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError = _descriptionError.asStateFlow()

    // Location structured data
    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var currentCountry: String = ""
    private var currentRegion: String = ""
    private var currentCity: String = ""
    private var currentExactAddress: String = ""

    fun onTitleChanged(title: String) {
        _titleError.value = when {
            title.isBlank() -> "El título es obligatorio"
            title.length < 5 -> "El título debe tener al menos 5 caracteres"
            else -> null
        }
    }

    fun onDescriptionChanged(desc: String) {
        _descriptionError.value = when {
            desc.isBlank() -> "La descripción es obligatoria"
            desc.length < 20 -> "Por favor describe mejor tu servicio (mín. 20 caracteres)"
            else -> null
        }
    }

    fun onPricesChanged(min: String, max: String) {
        val minVal = min.toDoubleOrNull() ?: 0.0
        val maxVal = max.toDoubleOrNull() ?: 0.0
        
        _priceError.value = when {
            minVal <= 0 || maxVal <= 0 -> "Ingresa precios válidos"
            minVal >= maxVal -> "El precio mínimo debe ser menor al máximo"
            else -> null
        }
    }

    fun setLocationData(lat: Double, lng: Double, country: String, region: String, city: String, exact: String) {
        currentLat = lat
        currentLng = lng
        currentCountry = country
        currentRegion = region
        currentCity = city
        currentExactAddress = exact
    }

    fun uploadImage(uri: Uri) {
        if (_uploadedImages.value.size >= 3) {
            _error.value = "Solo puedes subir hasta 3 imágenes"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            val result = storageService.uploadImage(uri)
            result.onSuccess { resource ->
                val isFirst = _uploadedImages.value.isEmpty()
                _uploadedImages.update { it + resource.copy(isPrimary = isFirst) }
            }.onFailure { e ->
                _error.value = "Fallo al cargar imagen: ${e.message}"
            }
            _isUploading.value = false
        }
    }

    fun selectPrimaryImage(index: Int) {
        _uploadedImages.update { list ->
            list.mapIndexed { i, img -> img.copy(isPrimary = i == index) }
        }
    }

    fun removeImage(index: Int) {
        _uploadedImages.update { list ->
            val newList = list.filterIndexed { i, _ -> i != index }
            if (newList.isNotEmpty() && list[index].isPrimary) {
                newList.mapIndexed { i, img -> if (i == 0) img.copy(isPrimary = true) else img }
            } else newList
        }
    }

    fun saveServicePost(
        titulo: String,
        categoria: String,
        descripcion: String,
        precioMin: Double,
        precioMax: Double,
        fullAddress: String,
        radio: Double,
        onSuccess: () -> Unit
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Final Validation check
        if (_titleError.value != null || _priceError.value != null || _descriptionError.value != null) {
            _error.value = "Por favor corrige los errores en el formulario"
            return
        }

        if (_uploadedImages.value.isEmpty()) {
            _error.value = "Debes subir al menos una imagen"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val newPost = ServicePost(
                authorId = currentUserId,
                title = titulo,
                category = try { ServiceCategory.valueOf(categoria.uppercase()) } catch(e: Exception) { ServiceCategory.HOGAR },
                description = descripcion,
                images = _uploadedImages.value,
                coordinates = GeoPoint(currentLat, currentLng),
                addressText = fullAddress,
                country = currentCountry,
                region = currentRegion,
                city = currentCity,
                exactAddress = currentExactAddress,
                coverageRadius = radio,
                minPrice = precioMin,
                maxPrice = precioMax,
                status = PostStatus.PENDIENTE
            )

            val result = serviceRepository.saveServicePost(newPost)
            result.onSuccess {
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = currentUserId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "Tu servicio '$titulo' ha sido enviado para revisión."
                    )
                )
                _successMessage.value = "¡Servicio creado exitosamente! Un moderador lo revisará pronto."
                onSuccess()
            }.onFailure { e ->
                _error.value = "Error al guardar: ${e.message}"
            }
            _isSaving.value = false
        }
    }
    
    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }
}
