package com.example.ruvo_app.features.service

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.ImageStorageService
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
    private val serviceRepository: ServiceRepository
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

    fun onTitleChanged(title: String) {
        if (title.isBlank()) {
            _titleError.value = "El título es obligatorio"
        } else if (title.length < 5) {
            _titleError.value = "El título es muy corto"
        } else {
            _titleError.value = null
        }
    }

    fun onPricesChanged(min: String, max: String) {
        val minVal = min.toDoubleOrNull() ?: 0.0
        val maxVal = max.toDoubleOrNull() ?: 0.0
        
        if (minVal > 0 && maxVal > 0 && minVal >= maxVal) {
            _priceError.value = "El precio mínimo debe ser menor al máximo"
        } else {
            _priceError.value = null
        }
    }

    fun uploadImage(uri: Uri) {
        if (_uploadedImages.value.size >= 3) {
            _error.value = "Solo puedes subir hasta 3 imágenes"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            
            val result = storageService.uploadImage(uri)
            
            result.onSuccess { resource ->
                // If it's the first image, make it primary
                val isFirst = _uploadedImages.value.isEmpty()
                _uploadedImages.update { it + resource.copy(isPrimary = isFirst) }
                _isUploading.value = false
            }.onFailure { e ->
                _error.value = "Fallo al cargar imagen: ${e.message}"
                _isUploading.value = false
            }
        }
    }

    fun selectPrimaryImage(index: Int) {
        _uploadedImages.update { list ->
            list.mapIndexed { i, img ->
                img.copy(isPrimary = i == index)
            }
        }
    }

    fun removeImage(index: Int) {
        _uploadedImages.update { list ->
            val newList = list.filterIndexed { i, _ -> i != index }
            // If we removed the primary image, set the first one as primary
            if (newList.isNotEmpty() && list[index].isPrimary) {
                newList.mapIndexed { i, img -> if (i == 0) img.copy(isPrimary = true) else img }
            } else {
                newList
            }
        }
    }

    fun saveServicePost(
        titulo: String,
        categoria: String,
        descripcion: String,
        precioMin: Double,
        precioMax: Double,
        ubicacion: String,
        radio: Double,
        onSuccess: () -> Unit
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        // Final validation
        if (titulo.isBlank() || _titleError.value != null) {
            _error.value = "Por favor corrige el título"
            return
        }

        if (_uploadedImages.value.isEmpty()) {
            _error.value = "Debes subir al menos una imagen"
            return
        }

        if (precioMin <= 0 || precioMax <= 0 || precioMin >= precioMax || _priceError.value != null) {
            _error.value = "Valores de precio no válidos"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            val newPost = ServicePost(
                authorId = currentUserId,
                title = titulo,
                category = try { ServiceCategory.valueOf(categoria.uppercase()) } catch(e: Exception) { ServiceCategory.HOGAR },
                description = descripcion,
                images = _uploadedImages.value,
                coordinates = GeoPoint(0.0, 0.0),
                addressText = ubicacion,
                coverageRadius = radio,
                minPrice = precioMin,
                maxPrice = precioMax,
                status = PostStatus.PENDIENTE
            )

            val result = serviceRepository.saveServicePost(newPost)
            
            _isSaving.value = false
            result.onSuccess {
                _successMessage.value = "¡Servicio creado exitosamente!"
                onSuccess()
            }.onFailure { e ->
                _error.value = "Error al guardar en Firestore: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
