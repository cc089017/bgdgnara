package com.example.bgjz_app.data.mock

import androidx.annotation.DrawableRes
import com.example.bgjz_app.R

enum class ProductStatus { ON_SALE, RESERVED, SOLD }

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val status: ProductStatus = ProductStatus.ON_SALE,
    val isLightningPay: Boolean = false,
    val isLiked: Boolean = false,
    val sellerId: String = "bgjz_user",
    /** 백엔드 연결 시 사용. null이면 imageRes(로컬 drawable)로 fallback. */
    val thumbnailUrl: String? = null,
)

data class User(
    val nickname: String,
    val email: String,
    @DrawableRes val avatarRes: Int
)

data class BannerItem(
    val id: Int,
    @DrawableRes val imageRes: Int,
    val title: String
)

data class ChatMessage(
    val id: Int,
    val senderId: String, // "me" = 나, 그 외 = sellerId
    val content: String,
    val timestamp: String
)

data class ChatRoom(
    val id: Int,
    val productId: Int,
    val otherUser: Seller,
    val unreadCount: Int = 0
)

data class Seller(
    val id: String,
    val nickname: String,
    val region: String,
    @DrawableRes val avatarRes: Int,
    val mannerScore: Float = 36.5f
)

object MockData {

    val currentUser = User(
        nickname = "번개유저",
        email = "user@bgjz.com",
        avatarRes = R.drawable.ic_launcher_foreground
    )

    val sellers: List<Seller> = listOf(
        Seller("bgjz_user", "번개유저", "서울 강남구", R.drawable.ic_launcher_foreground, 38.2f),
        Seller("minjun123", "중고왕민준", "서울 마포구", R.drawable.ic_launcher_foreground, 36.5f),
        Seller("savvy_buyer", "알뜰소비자", "경기 성남시", R.drawable.ic_launcher_foreground, 40.1f),
        Seller("flash_deal", "번개빠른거래", "서울 송파구", R.drawable.ic_launcher_foreground, 35.0f)
    )

    val banners: List<BannerItem> = listOf(
        BannerItem(1, R.drawable.ic_launcher_background, "지금 핫한 아이템"),
        BannerItem(2, R.drawable.ic_launcher_background, "번개페이 이벤트"),
        BannerItem(3, R.drawable.ic_launcher_background, "신상품 소개")
    )

    // sellerId: 각 상품의 판매자 (sellers 리스트의 id)
    val products: List<Product> = listOf(
        Product(1, "아이폰 15 프로", 1_200_000, R.drawable.ic_launcher_background, isLightningPay = true, sellerId = "minjun123"),
        Product(2, "맥북 에어 M2", 1_500_000, R.drawable.ic_launcher_background, ProductStatus.RESERVED, sellerId = "savvy_buyer"),
        Product(3, "에어팟 프로 2세대", 250_000, R.drawable.ic_launcher_background, isLightningPay = true, isLiked = true, sellerId = "minjun123"),
        Product(4, "닌텐도 스위치 OLED", 350_000, R.drawable.ic_launcher_background, ProductStatus.SOLD, sellerId = "flash_deal"),
        Product(5, "갤럭시 워치 6", 280_000, R.drawable.ic_launcher_background, isLiked = true, sellerId = "savvy_buyer"),
        Product(6, "다이슨 청소기 V12", 600_000, R.drawable.ic_launcher_background, isLightningPay = true, sellerId = "flash_deal"),
        Product(7, "스타벅스 텀블러", 25_000, R.drawable.ic_launcher_background, sellerId = "minjun123"),
        Product(8, "캠핑 의자 세트", 80_000, R.drawable.ic_launcher_background, ProductStatus.RESERVED, isLiked = true, sellerId = "savvy_buyer")
    )

    val likedProducts: List<Product> get() = products.filter { it.isLiked }

    val myProducts: List<Product> = products.take(6)

    val chatRooms: List<ChatRoom> by lazy {
        listOf(
            ChatRoom(id = 1, productId = 1, otherUser = sellers[1], unreadCount = 0),
            ChatRoom(id = 2, productId = 3, otherUser = sellers[2], unreadCount = 2),
            ChatRoom(id = 3, productId = 6, otherUser = sellers[3], unreadCount = 1)
        )
    }

    val chatMessages: Map<Int, List<ChatMessage>> = mapOf(
        1 to listOf(
            ChatMessage(1, "minjun123", "안녕하세요, 아직 판매 중인가요?", "오전 10:23"),
            ChatMessage(2, "me", "네, 판매 중입니다!", "오전 10:25"),
            ChatMessage(3, "minjun123", "직거래 가능한가요?", "오전 10:26"),
            ChatMessage(4, "me", "네 강남역 근처 가능합니다 :)", "오전 10:30"),
            ChatMessage(5, "minjun123", "좋아요! 가격 조금 네고 가능할까요?", "오전 10:31")
        ),
        2 to listOf(
            ChatMessage(6, "savvy_buyer", "에어팟 상태 어떤가요?", "어제"),
            ChatMessage(7, "me", "깨끗하게 사용했어요, 케이스도 있어요", "어제"),
            ChatMessage(8, "savvy_buyer", "얼마나 쓰셨나요?", "어제"),
            ChatMessage(9, "savvy_buyer", "직거래만 되나요?", "어제")
        ),
        3 to listOf(
            ChatMessage(10, "flash_deal", "다이슨 청소기 아직 있나요?", "3일 전")
        )
    )
}
