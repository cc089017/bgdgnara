package com.example.bgjz_app.ui.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.mock.MockProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistUiState(
    val isLoading: Boolean = false,
    val likedProducts: List<Product> = emptyList(),
    val error: String? = null
)

// 백엔드 연결 시: MockProductRepository() → RemoteProductRepository(retrofit)
class WishlistViewModel(
    private val repository: ProductRepository = MockProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        loadLikedProducts()
    }

    fun loadLikedProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getLikedProducts()) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, likedProducts = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun unlike(productId: Int) {
        viewModelScope.launch {
            repository.unlikeProduct(productId)
            loadLikedProducts()
        }
    }
}
