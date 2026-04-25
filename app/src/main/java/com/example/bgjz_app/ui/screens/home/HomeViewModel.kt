package com.example.bgjz_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repository: ProductRepository = RemoteProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getProducts(limit = HOME_PRODUCT_LIMIT)) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, products = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    companion object {
        private const val HOME_PRODUCT_LIMIT = 6
    }

    fun toggleLike(product: Product) {
        viewModelScope.launch {
            if (product.isLiked) repository.unlikeProduct(product.id)
            else repository.likeProduct(product.id)
            loadProducts()
        }
    }
}
