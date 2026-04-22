package com.example.bgjz_app.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.model.RegisterProductRequest
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.mock.MockProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductEditUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val price: Int = 0,
    val description: String = "",
    val category: String = ""
)

// 백엔드 연결 시: MockProductRepository() → RemoteProductRepository(retrofit)
class ProductEditViewModel(
    private val repository: ProductRepository = MockProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductEditUiState())
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getProductById(productId)) {
                is UserResult.Success -> {
                    val p = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = p.name,
                            price = p.price,
                            description = p.description,
                            category = p.category
                        )
                    }
                }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun updateProduct(productId: Int, name: String, price: String, description: String, category: String) {
        val parsedPrice = price.filter { it.isDigit() }.toIntOrNull() ?: 0
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "상품명을 입력해주세요") }
            return
        }
        if (parsedPrice <= 0) {
            _uiState.update { it.copy(error = "올바른 가격을 입력해주세요") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = RegisterProductRequest(
                name = name,
                price = parsedPrice,
                category = category,
                description = description
            )
            when (repository.updateProduct(productId, request)) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = "수정에 실패했습니다") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
