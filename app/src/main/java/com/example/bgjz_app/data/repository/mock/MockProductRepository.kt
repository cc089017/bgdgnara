package com.example.bgjz_app.data.repository.mock

import com.example.bgjz_app.R
import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.data.model.RegisterProductRequest
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import kotlinx.coroutines.delay

class MockProductRepository : ProductRepository {

    // 찜 상태를 Mock에서 관리 (백엔드 연결 시 서버가 관리)
    private val likedIds = MockData.products
        .filter { it.isLiked }
        .map { it.id }
        .toMutableSet()

    private val myProducts = MockData.myProducts.toMutableList()

    override suspend fun getProductById(productId: Int): UserResult<ProductDetail> {
        delay(400)
        val product = MockData.products.find { it.id == productId }
            ?: return UserResult.Error("상품을 찾을 수 없습니다")
        val descriptions = mapOf(
            1 to "정말 깨끗하게 사용했습니다. 케이스 포함 드립니다. 직거래 우선이며 택배도 가능합니다.",
            2 to "M2 맥북에어 스페이스그레이 8GB 256GB입니다. 충전기, 박스 모두 있습니다.",
            3 to "에어팟 프로 2세대 MagSafe 케이스 모델입니다. 노이즈 캔슬링 완벽 작동합니다.",
            4 to "닌텐도 스위치 OLED 화이트 색상입니다. 게임 타이틀 2개 포함합니다.",
            5 to "갤럭시 워치 6 44mm 실버입니다. 스트랩 여분 1개 드립니다.",
            6 to "다이슨 V12 디텍트 슬림 모델입니다. 흡입력 완벽합니다.",
            7 to "스타벅스 써머 텀블러 2024 한정판입니다. 한 번도 사용 안 했습니다.",
            8 to "코베아 캠핑 의자 2개 세트입니다. 2회 사용했으며 상태 매우 좋습니다."
        )
        val categories = mapOf(1 to "디지털/가전", 2 to "디지털/가전", 3 to "디지털/가전",
            4 to "취미/게임", 5 to "디지털/가전", 6 to "생활가전", 7 to "생활용품", 8 to "스포츠/레저")
        val times = listOf("방금 전", "5분 전", "23분 전", "1시간 전", "2시간 전", "3시간 전", "어제", "2일 전")
        val seller = MockData.sellers.find { it.id == product.sellerId } ?: MockData.sellers.first()
        return UserResult.Success(
            ProductDetail(
                id = product.id,
                name = product.name,
                price = product.price,
                imageRes = product.imageRes,
                status = product.status,
                isLightningPay = product.isLightningPay,
                isLiked = product.id in likedIds,
                description = descriptions[product.id] ?: "깨끗하게 사용했습니다. 문의 환영합니다.",
                category = categories[product.id] ?: "기타",
                viewCount = (product.id * 37 + 12),
                likeCount = likedIds.size + product.id,
                timeAgo = times[product.id % times.size],
                sellerId = seller.id,
                sellerNickname = seller.nickname,
                sellerRegion = seller.region,
                sellerAvatarRes = seller.avatarRes
            )
        )
    }

    override suspend fun getProducts(): UserResult<List<Product>> {
        delay(400)
        return UserResult.Success(
            MockData.products.map { it.copy(isLiked = it.id in likedIds) }
        )
    }

    override suspend fun getMyProducts(): UserResult<List<Product>> {
        delay(400)
        return UserResult.Success(myProducts.toList())
    }

    override suspend fun getLikedProducts(): UserResult<List<Product>> {
        delay(400)
        return UserResult.Success(
            MockData.products.filter { it.id in likedIds }
        )
    }

    override suspend fun registerProduct(request: RegisterProductRequest): UserResult<Product> {
        delay(800)
        if (request.name.isBlank() || request.price <= 0) {
            return UserResult.Error("상품명과 가격을 입력해주세요")
        }
        val newProduct = Product(
            id = MockData.products.size + myProducts.size + 1,
            name = request.name,
            price = request.price,
            imageRes = R.drawable.ic_launcher_background,
            status = ProductStatus.ON_SALE
        )
        myProducts.add(newProduct)
        return UserResult.Success(newProduct)
    }

    override suspend fun updateProduct(productId: Int, request: RegisterProductRequest): UserResult<Product> {
        delay(600)
        val index = myProducts.indexOfFirst { it.id == productId }
        if (index == -1) return UserResult.Error("상품을 찾을 수 없습니다")
        val updated = myProducts[index].copy(name = request.name, price = request.price)
        myProducts[index] = updated
        return UserResult.Success(updated)
    }

    override suspend fun deleteProduct(productId: Int): UserResult<Unit> {
        delay(400)
        val removed = myProducts.removeIf { it.id == productId }
        return if (removed) UserResult.Success(Unit) else UserResult.Error("상품을 찾을 수 없습니다")
    }

    override suspend fun updateProductStatus(productId: Int, status: ProductStatus): UserResult<Unit> {
        delay(300)
        val index = myProducts.indexOfFirst { it.id == productId }
        if (index == -1) return UserResult.Error("상품을 찾을 수 없습니다")
        myProducts[index] = myProducts[index].copy(status = status)
        return UserResult.Success(Unit)
    }

    override suspend fun uploadProductImages(productId: Int, imageUris: List<String>): UserResult<Unit> {
        delay(600)
        return UserResult.Success(Unit)
    }

    override suspend fun getProductsByUser(userId: String): UserResult<List<Product>> {
        delay(400)
        val userProducts = MockData.products.filter { it.sellerId == userId }
        return UserResult.Success(userProducts.map { it.copy(isLiked = it.id in likedIds) })
    }

    override suspend fun likeProduct(productId: Int): UserResult<Unit> {
        delay(300)
        likedIds.add(productId)
        return UserResult.Success(Unit)
    }

    override suspend fun unlikeProduct(productId: Int): UserResult<Unit> {
        delay(300)
        likedIds.remove(productId)
        return UserResult.Success(Unit)
    }
}
