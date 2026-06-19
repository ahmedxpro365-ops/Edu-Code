package com.educode.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.di.AppModule
import com.educode.app.domain.models.User
import com.educode.app.domain.models.ProgressSystem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUIState(
    val user: User = User(name = "البطل المبرمج", xp = 0, coins = 100, hearts = 5),
    val nextHeartCountdown: String = "",
    val isLoading: Boolean = true
)

sealed class HomeEvent {
    data class HeartRestored(val currentHearts: Int) : HomeEvent()
    data class LevelUp(val newRank: String, val arabicTitle: String) : HomeEvent()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private var previousUser: User? = null
    private var timerJob: Job? = null

    init {
        loadProfileAndStartTimer()
    }

    private fun loadProfileAndStartTimer() {
        viewModelScope.launch {
            val userId = AppModule.authRepository.getCurrentUserId()
            if (userId != null) {
                AppModule.userRepository.getUserProfileFlow(userId).collect { user ->
                    if (user != null) {
                        // Check for Level Up / Rank Upgrade
                        previousUser?.let { prev ->
                            val prevRank = ProgressSystem.getRankInfoForXp(prev.xp)
                            val currentRank = ProgressSystem.getRankInfoForXp(user.xp)
                            if (currentRank.minXp > prevRank.minXp) {
                                triggerLevelUpReward(user.id, currentRank.bonusCoinsReward)
                                _events.emit(HomeEvent.LevelUp(currentRank.rank, currentRank.arabicTitle))
                            }
                            if (user.hearts > prev.hearts) {
                                _events.emit(HomeEvent.HeartRestored(user.hearts))
                            }
                        }

                        previousUser = user
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false
                        )
                        startCountdownTimer(user)
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun triggerLevelUpReward(userId: String, bonusCoins: Int) {
        if (bonusCoins <= 0) return
        viewModelScope.launch {
            AppModule.userRepository.updateXPAndCoins(userId, 0, bonusCoins)
        }
    }

    private fun startCountdownTimer(user: User) {
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                
                // Heart Countdown
                val heartPart = if (user.hearts < 5 && user.lastHeartRestoreTime != 0L) {
                    val targetHeartTime = user.lastHeartRestoreTime + (2 * 60 * 60 * 1000)
                    val diffH = targetHeartTime - currentTime
                    if (diffH <= 0) "00:00" 
                    else {
                        val m = (diffH / (1000 * 60)) % 60
                        val s = (diffH / 1000) % 60
                        String.format("%02d:%02d", m, s)
                    }
                } else ""

                _uiState.value = _uiState.value.copy(
                    nextHeartCountdown = heartPart
                )
                
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
