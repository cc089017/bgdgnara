package com.example.bgjz_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.BannerRepository
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteBannerRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val productRepository: ProductRepository = RemoteProductRepository(),
    private val bannerRepository: BannerRepository = RemoteBannerRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadBanners()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = productRepository.getProducts(limit = HOME_PRODUCT_LIMIT)) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, products = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun loadBanners() {
        viewModelScope.launch {
            when (val result = bannerRepository.getActiveBanners()) {
                is UserResult.Success -> _uiState.update { it.copy(banners = result.data) }
                is UserResult.Error -> Unit  // 배너 실패는 화면 전체 에러로 띄우지 않음
            }
        }
    }

    companion object {
        private const val HOME_PRODUCT_LIMIT = 6
    }

    fun toggleLike(product: Product) {
        viewModelScope.launch {
            if (product.isLiked) productRepository.unlikeProduct(product.id)
            else productRepository.likeProduct(product.id)
            loadProducts()
        }
    }
}
