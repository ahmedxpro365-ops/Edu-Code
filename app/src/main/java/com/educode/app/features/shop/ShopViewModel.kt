package com.educode.app.features.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educode.app.di.AppModule
import com.educode.app.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopUIState(
    val items: List<ShopItem> = emptyList(),
    val user: User? = null,
    val selectedCategory: ItemCategory = ItemCategory.THEME,
    val isLoading: Boolean = true,
    val error: String? = null,
    val purchaseSuccess: Boolean = false
)

class ShopViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShopUIState())
    val uiState: StateFlow<ShopUIState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = AppModule.authRepository.getCurrentUserId() ?: return@launch
            
            val userResult = AppModule.userRepository.getUserProfile(userId)
            val itemsResult = AppModule.shopRepository.getShopItems()
            
            userResult.onSuccess { user ->
                itemsResult.onSuccess { items ->
                    _uiState.update { it.copy(
                        user = user,
                        items = items,
                        isLoading = false
                    ) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectCategory(category: ItemCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun purchaseItem(item: ShopItem) {
        viewModelScope.launch {
            val userId = _uiState.value.user?.id ?: return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = AppModule.shopRepository.purchaseItem(userId, item)
            
            result.onSuccess {
                _uiState.update { it.copy(purchaseSuccess = true, isLoading = false) }
                loadData() // Refresh user data
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun equipItem(item: ShopItem) {
        viewModelScope.launch {
            val userId = _uiState.value.user?.id ?: return@launch
            AppModule.shopRepository.equipItem(userId, item)
            loadData()
        }
    }

    fun unequipItem(category: ItemCategory) {
        viewModelScope.launch {
            val userId = _uiState.value.user?.id ?: return@launch
            AppModule.shopRepository.unequipItem(userId, category)
            loadData()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(purchaseSuccess = false) }
    }
}
