package com.example.bgjz_app.ui.screens.product

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.model.RegisterProductRequest
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
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

class ProductRegisterViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, RemoteProductRepository())

    private val _uiState = MutableStateFlow(ProductRegisterUiState())
    val uiState: StateFlow<ProductRegisterUiState> = _uiState.asStateFlow()

    fun register(
        name: String,
        description: String,
        category: String,
        price: Int,
        shippingIncluded: Boolean,
        autoPriceDown: Boolean,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = RegisterProductRequest(
                name = name,
                category = category,
                price = price,
                shippingIncluded = shippingIncluded,
                autoPriceDown = autoPriceDown,
                description = description,
            )
            when (val result = repository.registerProduct(request)) {
                is UserResult.Success -> {
                    if (imageUris.isNotEmpty()) {
                        val imageBytes = imageUris.mapNotNull { uri ->
                            getApplication<Application>().contentResolver
                                .openInputStream(uri)?.use { it.readBytes() }
                        }
                        when (val uploadResult = repository.uploadProductImages(result.data.id, imageBytes)) {
                            is UserResult.Success -> Unit
                            is UserResult.Error -> {
                                _uiState.update { it.copy(isLoading = false, error = "이미지 업로드 실패: ${uploadResult.message}") }
                                return@launch
                            }
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
