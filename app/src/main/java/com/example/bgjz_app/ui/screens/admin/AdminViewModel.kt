package com.example.bgjz_app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.BannerRepository
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.UserRepository
import com.example.bgjz_app.data.repository.remote.RemoteBannerRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val accessDenied: Boolean = false,
    val error: String? = null,
    val message: String? = null,

    // 본인 ID — 자기 권한 토글 차단용 (백엔드도 막지만 UI에서도 막음)
    val currentUserId: String = "",

    // 유저 탭
    val users: List<UserProfile> = emptyList(),
    val userQuery: String = "",

    // 상품 탭
    val products: List<Product> = emptyList(),
    val productQuery: String = "",
    val productStatusFilter: ProductStatus? = null,

    // 배너 탭
    val banners: List<Banner> = emptyList(),
)

class AdminViewModel(
    private val userRepository: UserRepository = RemoteUserRepository(),
    private val productRepository: ProductRepository = RemoteProductRepository(),
    private val bannerRepository: BannerRepository = RemoteBannerRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        verifyAdminAndLoad()
    }

    /** 진입 시 본인이 관리자인지 먼저 확인. 아니면 모든 데이터 로드 차단. */
    private fun verifyAdminAndLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = userRepository.getMyProfile()) {
                is UserResult.Success -> {
                    val me = result.data
                    if (!me.isAdmin) {
                        _uiState.update {
                            it.copy(isLoading = false, accessDenied = true)
                        }
                        return@launch
                    }
                    _uiState.update { it.copy(currentUserId = me.id) }
                    loadAll()
                }
                is UserResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    private fun loadAll() {
        loadUsers()
        loadProducts()
        loadBanners()
    }

    // ── 유저 탭 ────────────────────────────────────────────────

    fun loadUsers() {
        viewModelScope.launch {
            when (val result = userRepository.getAllUsers()) {
                is UserResult.Success -> _uiState.update {
                    it.copy(isLoading = false, users = result.data)
                }
                is UserResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun onUserQueryChange(query: String) {
        _uiState.update { it.copy(userQuery = query) }
    }

    /** 본인 권한은 토글 못 함 (백엔드도 400 반환). */
    fun toggleUserAdmin(userId: String) {
        if (userId == _uiState.value.currentUserId) {
            _uiState.update { it.copy(error = "본인의 관리자 권한은 변경할 수 없습니다") }
            return
        }
        viewModelScope.launch {
            when (val result = userRepository.toggleUserAdmin(userId)) {
                is UserResult.Success -> {
                    val updated = result.data
                    _uiState.update { state ->
                        state.copy(
                            users = state.users.map { if (it.id == updated.id) updated else it },
                            message = if (updated.isAdmin) "${updated.nickname}님에게 관리자 권한 부여"
                                     else "${updated.nickname}님 관리자 권한 해제"
                        )
                    }
                }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun filteredUsers(): List<UserProfile> {
        val state = _uiState.value
        if (state.userQuery.isBlank()) return state.users
        val q = state.userQuery
        return state.users.filter {
            it.nickname.contains(q, ignoreCase = true) ||
            it.id.contains(q, ignoreCase = true) ||
            (it.region?.contains(q, ignoreCase = true) == true)
        }
    }

    // ── 상품 탭 ────────────────────────────────────────────────

    fun loadProducts() {
        viewModelScope.launch {
            when (val result = productRepository.getProducts(limit = PRODUCT_LIMIT)) {
                is UserResult.Success -> _uiState.update { it.copy(products = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun onProductQueryChange(query: String) {
        _uiState.update { it.copy(productQuery = query) }
        searchProducts(query)
    }

    private fun searchProducts(query: String) {
        viewModelScope.launch {
            val result = if (query.isBlank()) {
                productRepository.getProducts(limit = PRODUCT_LIMIT)
            } else {
                productRepository.searchProducts(query)
            }
            when (result) {
                is UserResult.Success -> _uiState.update { it.copy(products = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun setProductStatusFilter(status: ProductStatus?) {
        _uiState.update { it.copy(productStatusFilter = status) }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            when (val result = productRepository.deleteProduct(productId)) {
                is UserResult.Success -> _uiState.update { state ->
                    state.copy(
                        products = state.products.filter { it.id != productId },
                        message = "상품이 삭제되었습니다",
                    )
                }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun filteredProducts(): List<Product> {
        val state = _uiState.value
        return state.products.filter {
            state.productStatusFilter == null || it.status == state.productStatusFilter
        }
    }

    // ── 배너 탭 ────────────────────────────────────────────────

    fun loadBanners() {
        viewModelScope.launch {
            when (val result = bannerRepository.getActiveBanners()) {
                is UserResult.Success -> _uiState.update { it.copy(banners = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun createBanner(imageUrl: String, title: String?, linkUrl: String?) {
        if (imageUrl.isBlank()) {
            _uiState.update { it.copy(error = "이미지 URL을 입력해주세요") }
            return
        }
        viewModelScope.launch {
            val result = bannerRepository.createBanner(
                imageUrl = imageUrl.trim(),
                title = title?.trim()?.ifBlank { null },
                linkUrl = linkUrl?.trim()?.ifBlank { null },
            )
            when (result) {
                is UserResult.Success -> {
                    _uiState.update { it.copy(message = "배너가 등록되었습니다") }
                    loadBanners()
                }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun deleteBanner(bannerId: Int) {
        viewModelScope.launch {
            when (val result = bannerRepository.deleteBanner(bannerId)) {
                is UserResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            banners = state.banners.filter { it.id != bannerId },
                            message = "배너가 삭제되었습니다",
                        )
                    }
                }
                is UserResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    // ── 메시지/에러 dismiss ────────────────────────────────────

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val PRODUCT_LIMIT = 100
    }
}
