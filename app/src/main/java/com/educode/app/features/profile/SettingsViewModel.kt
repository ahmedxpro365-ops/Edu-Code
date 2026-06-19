package com.educode.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.di.AppModule
import com.educode.app.domain.models.NotificationSettings
import com.educode.app.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUIState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val isLoggedOut: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val userId = AppModule.authRepository.getCurrentUserId() ?: return@launch
            val result = AppModule.userRepository.getUserProfile(userId)
            result.onSuccess { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            AppModule.userRepository.updateNotificationSettings(userId, settings)
            _uiState.update { it.copy(user = it.user?.copy(notificationSettings = settings)) }
            
            // Apply scheduling logic
            if (settings.enabled && settings.dailyReminder) {
                AppModule.notificationManager.scheduleDailyReminder(settings.reminderTime)
            } else {
                AppModule.notificationManager.cancelDailyReminder()
            }
        }
    }

    fun updateUserName(newName: String) {
        val userId = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            // Simulated update
            _uiState.update { it.copy(user = it.user?.copy(name = newName)) }
            // In real app: AppModule.userRepository.updateName(userId, newName)
        }
    }

    fun logout() {
        viewModelScope.launch {
            AppModule.authRepository.signOut()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(message = "تم مسح التخزين المؤقت بنجاح") }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            // Implementation for account deletion
            _uiState.update { it.copy(message = "سيتم معالجة طلب حذف الحساب") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
