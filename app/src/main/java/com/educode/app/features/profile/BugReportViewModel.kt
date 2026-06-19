package com.educode.app.features.profile

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.EduCodeApplication
import com.educode.app.di.AppModule
import com.educode.app.domain.models.BugReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BugReportUIState(
    val title: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class BugReportViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BugReportUIState())
    val uiState: StateFlow<BugReportUIState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun submitReport() {
        val state = _uiState.value
        if (state.title.isBlank() || state.description.isBlank()) {
            _uiState.update { it.copy(error = "يرجى ملء جميع الحقول المطلوبة") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            
            val userId = AppModule.authRepository.getCurrentUserId() ?: "anonymous"
            val appVersion = getAppVersion()
            
            val report = BugReport(
                userId = userId,
                title = state.title,
                description = state.description,
                appVersion = appVersion,
                androidVersion = Build.VERSION.RELEASE,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                timestamp = System.currentTimeMillis()
            )

            val result = AppModule.bugReportRepository.submitReport(report)
            result.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "فشل إرسال البلاغ") }
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = getApplication<EduCodeApplication>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            pInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
