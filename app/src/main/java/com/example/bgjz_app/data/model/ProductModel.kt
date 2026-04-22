package com.example.bgjz_app.data.model

import androidx.annotation.DrawableRes
import com.example.bgjz_app.data.mock.ProductStatus

data class RegisterProductRequest(
    val name: String,
    val category: String,
    val price: Int,
    val shippingIncluded: Boolean = false,
    val autoPriceDown: Boolean = false,
    val description: String = ""
)

data class ProductDetail(
    val id: Int,
    val name: String,
    val price: Int,
    @DrawableRes val imageRes: Int,
    val status: ProductStatus,
    val isLightningPay: Boolean,
    val isLiked: Boolean,
    val description: String,
    val category: String,
    val viewCount: Int,
    val likeCount: Int,
    val timeAgo: String,
    val sellerNickname: String,
    val sellerRegion: String,
    @DrawableRes val sellerAvatarRes: Int
)
