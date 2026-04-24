package com.example.bgjz_app.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val product: ProductDetail? = null,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val isActionLoading: Boolean = false
)

class ProductDetailViewModel(
    private val repository: ProductRepository = RemoteProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getProductById(productId)) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, product = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun toggleLike() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            if (product.isLiked) repository.unlikeProduct(product.id)
            else repository.likeProduct(product.id)
            _uiState.update {
                it.copy(product = product.copy(
                    isLiked = !product.isLiked,
                    likeCount = if (product.isLiked) product.likeCount - 1 else product.likeCount + 1
                ))
            }
        }
    }

    fun deleteProduct() {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (repository.deleteProduct(productId)) {
                is UserResult.Success -> _uiState.update { it.copy(isActionLoading = false, isDeleted = true) }
                is UserResult.Error -> _uiState.update { it.copy(isActionLoading = false) }
            }
        }
    }

    fun updateStatus(status: ProductStatus) {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (repository.updateProductStatus(productId, status)) {
                is UserResult.Success -> _uiState.update {
                    it.copy(isActionLoading = false, product = it.product?.copy(status = status))
                }
                is UserResult.Error -> _uiState.update { it.copy(isActionLoading = false) }
            }
        }
    }
}
