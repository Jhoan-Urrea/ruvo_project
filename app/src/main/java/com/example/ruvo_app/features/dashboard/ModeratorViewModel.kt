package com.example.ruvo_app.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.*
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.example.ruvo_app.domain.repository.ReportRepository
import com.example.ruvo_app.domain.repository.ServiceRepository
import com.example.ruvo_app.domain.repository.ServiceRequestRepository
import com.example.ruvo_app.domain.repository.UserRepository
import com.example.ruvo_app.domain.service.PostStatusUpdate
import com.example.ruvo_app.domain.service.TrustOptimizerService
import com.example.ruvo_app.domain.usecase.ChangeUserRoleUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val userRepository: UserRepository,
    private val reportRepository: ReportRepository,
    private val serviceRequestRepository: ServiceRequestRepository,
    private val changeUserRoleUseCase: ChangeUserRoleUseCase,
    private val notificationRepository: NotificationRepository,
    private val trustOptimizerService: TrustOptimizerService
) : ViewModel() {

    private val _allPosts = MutableStateFlow<List<ServicePost>>(emptyList())
    val allPosts: StateFlow<List<ServicePost>> = _allPosts.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _allReports = MutableStateFlow<List<Report>>(emptyList())
    val allReports: StateFlow<List<Report>> = _allReports.asStateFlow()

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private val currentModeratorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                serviceRepository.getAllServicePosts(),
                userRepository.getAllUsers(),
                reportRepository.getReports(),
                serviceRequestRepository.getAllRequests()
            ) { posts, users, reports, requests ->
                _allPosts.value = posts
                _allUsers.value = users
                _allReports.value = reports
                
                AdminStats(
                    totalUsers = users.size,
                    totalServices = posts.size,
                    pendingPosts = posts.count { it.status == PostStatus.PENDIENTE },
                    highRiskPosts = posts.count { it.status == PostStatus.REVISION_MANUAL_PRIORITARIA },
                    mediumRiskPosts = posts.count { it.status == PostStatus.REVISION_MANUAL },
                    activeReports = reports.count { it.status == ReportStatus.PENDIENTE || it.status == ReportStatus.EN_REVISION },
                    totalRequests = requests.size,
                    pendingRequests = requests.count { it.status == RequestStatus.PENDIENTE }
                )
            }.collect { updatedStats ->
                _stats.value = updatedStats
            }
        }
    }

    fun reanalyzePost(postId: String) {
        val post = _allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
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
            }.onFailure { e ->
                _errorEvent.emit("Error en análisis IA: ${e.message}")
            }
            _isAnalyzing.value = false
        }
    }

    fun approvePost(postId: String) {
        val post = _allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            val result = serviceRepository.updatePostStatus(postId, PostStatus.VERIFICADO)
            if (result.isSuccess) {
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = post.authorId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "🚀 Tu servicio '${post.title}' ha sido verificado y ya es visible para todos."
                    )
                )
            }
        }
    }

    fun rejectPost(postId: String, reason: String) {
        val post = _allPosts.value.find { it.id == postId } ?: return
        viewModelScope.launch {
            val result = serviceRepository.rejectPost(postId, reason)
            if (result.isSuccess) {
                notificationRepository.sendNotification(
                    Notification(
                        receiverId = post.authorId,
                        type = NotificationType.ESTADO_ACTUALIZADO,
                        message = "⚠️ Tu servicio '${post.title}' no pudo ser publicado. Motivo: $reason"
                    )
                )
            }
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            changeUserRoleUseCase(userId, newRole)
        }
    }

    fun resolveReport(reportId: String, status: ReportStatus, note: String?) {
        viewModelScope.launch {
            reportRepository.updateReportStatus(reportId, status, note, currentModeratorId)
        }
    }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val totalServices: Int = 0,
    val pendingPosts: Int = 0,
    val highRiskPosts: Int = 0,
    val mediumRiskPosts: Int = 0,
    val activeReports: Int = 0,
    val totalRequests: Int = 0,
    val pendingRequests: Int = 0
)
