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
    val isLiked: Boolean = false
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

object MockData {

    val currentUser = User(
        nickname = "번개유저",
        email = "user@bgjz.com",
        avatarRes = R.drawable.ic_launcher_foreground
    )

    val banners: List<BannerItem> = listOf(
        BannerItem(1, R.drawable.ic_launcher_background, "지금 핫한 아이템"),
        BannerItem(2, R.drawable.ic_launcher_background, "번개페이 이벤트"),
        BannerItem(3, R.drawable.ic_launcher_background, "신상품 소개")
    )

    val products: List<Product> = listOf(
        Product(1, "아이폰 15 프로", 1_200_000, R.drawable.ic_launcher_background, isLightningPay = true),
        Product(2, "맥북 에어 M2", 1_500_000, R.drawable.ic_launcher_background, ProductStatus.RESERVED),
        Product(3, "에어팟 프로 2세대", 250_000, R.drawable.ic_launcher_background, isLightningPay = true, isLiked = true),
        Product(4, "닌텐도 스위치 OLED", 350_000, R.drawable.ic_launcher_background, ProductStatus.SOLD),
        Product(5, "갤럭시 워치 6", 280_000, R.drawable.ic_launcher_background, isLiked = true),
        Product(6, "다이슨 청소기 V12", 600_000, R.drawable.ic_launcher_background, isLightningPay = true),
        Product(7, "스타벅스 텀블러", 25_000, R.drawable.ic_launcher_background),
        Product(8, "캠핑 의자 세트", 80_000, R.drawable.ic_launcher_background, ProductStatus.RESERVED, isLiked = true)
    )

    val likedProducts: List<Product> get() = products.filter { it.isLiked }

    val myProducts: List<Product> = products.take(6)
}
