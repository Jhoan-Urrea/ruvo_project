package com.example.ruvo_app.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruvo_app.domain.model.Notification
import com.example.ruvo_app.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val currentUserId = auth.currentUser?.uid ?: ""

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNotifications(currentUserId).collect { list ->
                _notifications.value = list
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _notifications.value.filter { !it.isRead }.forEach {
                repository.markAsRead(it.id)
            }
        }
    }
}
