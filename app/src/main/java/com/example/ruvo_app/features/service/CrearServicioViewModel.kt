package com.example.ruvo_app.features.service

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.R
import com.example.ruvo_app.core.utils.UiText
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.ImageStorageService
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.service.Achievement
import com.example.ruvo_app.domain.service.GamificationService
import com.example.ruvo_app.domain.service.PostStatusUpdate
import com.example.ruvo_app.domain.service.TrustOptimizerService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrearServicioViewModel @Inject constructor(
    private val storageService: ImageStorageService,
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val gamificationService: GamificationService,
    private val trustOptimizerService: TrustOptimizerService
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _uploadedImages = MutableStateFlow<List<ImageResource>>(emptyList())
    val uploadedImages = _uploadedImages.asStateFlow()

    private val _error = MutableStateFlow<UiText?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<UiText?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _titleError = MutableStateFlow<UiText?>(null)
    val titleError = _titleError.asStateFlow()

    private val _priceError = MutableStateFlow<UiText?>(null)
    val priceError = _priceError.asStateFlow()

    private val _descriptionError = MutableStateFlow<UiText?>(null)
    val descriptionError = _descriptionError.asStateFlow()

    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var currentCountry: String = ""
    private var currentRegion: String = ""
    private var currentCity: String = ""
    private var currentExactAddress: String = ""

    fun onTitleChanged(title: String) {
        _titleError.value = when {
            title.isBlank() -> UiText.StringResource(R.string.error_required_fields)
            title.length < 5 -> UiText.StringResource(R.string.error_title_short)
            else -> null
        }
    }

    fun onDescriptionChanged(desc: String) {
        _descriptionError.value = when {
            desc.isBlank() -> UiText.StringResource(R.string.error_required_fields)
            desc.length < 20 -> UiText.StringResource(R.string.error_description_short)
            else -> null
        }
    }

    fun onPricesChanged(min: String, max: String) {
        val minVal = min.toDoubleOrNull() ?: 0.0
        val maxVal = max.toDoubleOrNull() ?: 0.0
        
        _priceError.value = when {
            minVal <= 0 || maxVal <= 0 -> UiText.StringResource(R.string.error_invalid_prices)
            minVal >= maxVal -> UiText.StringResource(R.string.error_min_price_higher)
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
            _error.value = UiText.StringResource(R.string.error_max_images)
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            val result = storageService.uploadImage(uri)
            result.onSuccess { resource ->
                val isFirst = _uploadedImages.value.isEmpty()
                _uploadedImages.update { it + resource.copy(isPrimary = isFirst) }
            }.onFailure { e ->
                _error.value = UiText.StringResource(R.string.error_upload_failed)
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
        
        if (_titleError.value != null || _priceError.value != null || _descriptionError.value != null) {
            _error.value = UiText.StringResource(R.string.error_form_invalid)
            return
        }

        if (_uploadedImages.value.isEmpty()) {
            _error.value = UiText.StringResource(R.string.error_no_images)
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            
            val userResult = userRepository.getUserProfile(currentUserId).first()
            val user = userResult.getOrNull()

            val newPost = ServicePost(
                authorId = currentUserId,
                authorName = user?.fullName ?: "Proveedor",
                authorProfilePictureUrl = user?.profilePictureUrl,
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
            result.onSuccess { generatedId ->
                _successMessage.value = UiText.StringResource(R.string.success_service_sent)
                onSuccess()
                
                gamificationService.checkAndAwardAchievement(currentUserId, Achievement.Emprendedor)

                val savedPost = newPost.copy(id = generatedId)
                viewModelScope.launch {
                    analyzePostTrust(savedPost)
                }
                
            }.onFailure { e ->
                _error.value = UiText.StringResource(R.string.error_save_failed)
            }
            _isSaving.value = false
        }
    }

    private suspend fun analyzePostTrust(post: ServicePost) {
        val analysisResult = trustOptimizerService.analyzePost(post)
        analysisResult.onSuccess { result ->
            val finalStatus = when(result.status) {
                PostStatusUpdate.APROBADO -> PostStatus.VERIFICADO
                PostStatusUpdate.REVISION_MANUAL -> PostStatus.REVISION_MANUAL
                PostStatusUpdate.REVISION_MANUAL_PRIORITARIA -> PostStatus.REVISION_MANUAL_PRIORITARIA
                PostStatusUpdate.RECHAZADO -> PostStatus.RECHAZADO
            }
            
            serviceRepository.updateTrustAnalysis(
                postId = post.id,
                score = result.score,
                textScore = result.textScore,
                imageScore = result.imageScore,
                analysis = result.summary,
                details = result.details,
                aiConfidence = result.aiConfidence,
                riskHighlights = result.riskHighlights,
                newStatus = finalStatus
            )
            
            if (finalStatus == PostStatus.VERIFICADO) {
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = post.authorId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "🚀 ¡Excelente calidad! Tu servicio '${post.title}' ha sido aprobado automáticamente por nuestro Optimizador de Confianza."
                    )
                )
            }
        }
    }
    
    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }
}
