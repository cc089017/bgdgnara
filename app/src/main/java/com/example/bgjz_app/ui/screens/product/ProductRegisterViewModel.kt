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

data class ProductRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

// 백엔드 연결 시: MockProductRepository() → RemoteProductRepository(retrofit)
class ProductRegisterViewModel(
    private val repository: ProductRepository = MockProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductRegisterUiState())
    val uiState: StateFlow<ProductRegisterUiState> = _uiState.asStateFlow()

    fun register(
        name: String,
        category: String,
        price: Int,
        shippingIncluded: Boolean,
        autoPriceDown: Boolean,
        imageUris: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = RegisterProductRequest(name, category, price, shippingIncluded, autoPriceDown)
            when (val result = repository.registerProduct(request)) {
                is UserResult.Success -> {
                    if (imageUris.isNotEmpty()) {
                        repository.uploadProductImages(result.data.id, imageUris)
                    }
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
