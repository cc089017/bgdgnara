package com.example.bgjz_app.data.repository.remote

import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.data.model.RegisterProductRequest
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.remote.ApiResult
import com.example.bgjz_app.data.remote.RetrofitClient
import com.example.bgjz_app.data.remote.api.ProductApi
import com.example.bgjz_app.data.remote.api.UserApi
import com.example.bgjz_app.data.remote.dto.ProductCreateDto
import com.example.bgjz_app.data.remote.dto.ProductStatusUpdateDto
import com.example.bgjz_app.data.remote.dto.ProductUpdateDto
import com.example.bgjz_app.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.bgjz_app.data.remote.toDomain
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.mock.MockProductRepository

/**
 * Phase 1 범위: 목록 조회 + 상세 조회 + 내 상품 + 유저별 상품 → 백엔드 연결.
 * 나머지(등록/수정/삭제/찜/검색/상태변경/이미지업로드) → Mock에 위임.
 * Phase 2에서 백엔드 구현되면 하나씩 Remote로 승격.
 */
class RemoteProductRepository(
    private val productApi: ProductApi = RetrofitClient.productApi,
    private val userApi: UserApi = RetrofitClient.userApi,
    private val mockFallback: MockProductRepository = MockProductRepository(),
) : ProductRepository {

    override suspend fun getProducts(limit: Int): UserResult<List<Product>> {
        return when (val result = safeApiCall { productApi.getProducts(limit = limit) }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun getProductById(productId: Int): UserResult<ProductDetail> {
        return when (val result = safeApiCall { productApi.getProductDetail(productId) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun getMyProducts(): UserResult<List<Product>> {
        return when (val result = safeApiCall { productApi.getMyProducts() }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun getProductsByUser(userId: String): UserResult<List<Product>> {
        return when (val result = safeApiCall { userApi.getUserProducts(userId) }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    // ── 아래는 백엔드 미구현 → Mock에 위임 (Phase 2에서 교체) ───

    override suspend fun getLikedProducts(): UserResult<List<Product>> =
        mockFallback.getLikedProducts()

    override suspend fun registerProduct(request: RegisterProductRequest): UserResult<Product> {
        val dto = ProductCreateDto(
            productTitle = request.name,
            productBody = request.description.ifBlank { null },
            productPrice = request.price,
            category = request.category,
        )
        return when (val result = safeApiCall { productApi.createProduct(dto) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun updateProduct(productId: Int, request: RegisterProductRequest): UserResult<Product> {
        val dto = ProductUpdateDto(
            productTitle = request.name.ifBlank { null },
            productBody = request.description.ifBlank { null },
            productPrice = request.price,
            category = request.category.ifBlank { null },
        )
        return when (val result = safeApiCall { productApi.updateProduct(productId, dto) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun deleteProduct(productId: Int): UserResult<Unit> {
        return when (val result = safeApiCall { productApi.deleteProduct(productId) }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun updateProductStatus(productId: Int, status: ProductStatus): UserResult<Unit> {
        val statusStr = when (status) {
            ProductStatus.ON_SALE -> "판매중"
            ProductStatus.RESERVED -> "예약중"
            ProductStatus.SOLD -> "판매완료"
        }
        return when (val result = safeApiCall {
            productApi.updateProductStatus(productId, ProductStatusUpdateDto(statusStr))
        }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun uploadProductImages(productId: Int, imageBytes: List<ByteArray>): UserResult<Unit> {
        if (imageBytes.isEmpty()) return UserResult.Success(Unit)
        val parts = imageBytes.mapIndexed { idx, bytes ->
            val body = bytes.toRequestBody("image/jpeg".toMediaType())
            MultipartBody.Part.createFormData("files", "image_$idx.jpg", body)
        }
        return when (val result = safeApiCall { productApi.uploadImages(productId, parts) }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun likeProduct(productId: Int): UserResult<Unit> =
        mockFallback.likeProduct(productId)

    override suspend fun unlikeProduct(productId: Int): UserResult<Unit> =
        mockFallback.unlikeProduct(productId)

    override suspend fun searchProducts(query: String): UserResult<List<Product>> {
        return when (val result = safeApiCall { productApi.getProducts(search = query) }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }
}
