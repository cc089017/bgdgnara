package com.example.bgjz_app.ui.screens.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.mock.Seller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdminUiState(
    val userQuery: String = "",
    val bannedUserIds: Set<String> = emptySet(),
    val productQuery: String = "",
    val productStatusFilter: ProductStatus? = null,
    val deletedProductIds: Set<Int> = emptySet(),
    val pendingBannerUris: List<Uri> = emptyList(),
    val uploadDone: Boolean = false
)

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // 백엔드 연결 시: admin_id 권한 체크 후 데이터 로드
    fun onUserQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(userQuery = query)
    }

    fun toggleBan(userId: String) {
        val banned = _uiState.value.bannedUserIds
        _uiState.value = _uiState.value.copy(
            bannedUserIds = if (userId in banned) banned - userId else banned + userId
        )
    }

    fun onProductQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(productQuery = query)
    }

    fun setProductStatusFilter(status: ProductStatus?) {
        _uiState.value = _uiState.value.copy(productStatusFilter = status)
    }

    fun deleteProduct(productId: Int) {
        _uiState.value = _uiState.value.copy(
            deletedProductIds = _uiState.value.deletedProductIds + productId
        )
    }

    fun addBannerUris(uris: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            pendingBannerUris = _uiState.value.pendingBannerUris + uris,
            uploadDone = false
        )
    }

    fun removeBannerUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            pendingBannerUris = _uiState.value.pendingBannerUris - uri
        )
    }

    fun uploadBanners() {
        // TODO: POST /admin/upload 연동
        _uiState.value = _uiState.value.copy(uploadDone = true)
    }

    fun filteredUsers(): List<Seller> {
        val query = _uiState.value.userQuery
        return if (query.isBlank()) MockData.sellers
        else MockData.sellers.filter {
            it.nickname.contains(query, ignoreCase = true) ||
            it.region.contains(query, ignoreCase = true)
        }
    }

    fun filteredProducts(): List<Product> {
        val state = _uiState.value
        return MockData.products
            .filter { it.id !in state.deletedProductIds }
            .filter { state.productQuery.isBlank() || it.name.contains(state.productQuery, ignoreCase = true) }
            .filter { state.productStatusFilter == null || it.status == state.productStatusFilter }
    }
}
