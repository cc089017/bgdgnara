package com.example.bgjz_app.data.remote

import com.example.bgjz_app.R
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.mock.ProductStatus
import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.ProductDetail
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.remote.dto.BannerResponseDto
import com.example.bgjz_app.data.remote.dto.ProductDetailResponseDto
import com.example.bgjz_app.data.remote.dto.ProductResponseDto
import com.example.bgjz_app.data.remote.dto.UserResponseDto

/**
 * DTO ↔ Domain 모델 변환.
 * 백엔드엔 없고 UI에만 쓰는 필드(isLightningPay, viewCount, timeAgo 등)는
 * Phase 1 범위에서 기본값으로 채움. 추후 백엔드 확장 시 매퍼만 수정.
 */

// ── Product ────────────────────────────────────────────────

private fun parseProductStatus(status: String): ProductStatus = when (status) {
    "예약중" -> ProductStatus.RESERVED
    "판매완료" -> ProductStatus.SOLD
    else -> ProductStatus.ON_SALE
}

/** 상대경로면 BASE_URL을 붙이고, 이미 절대 URL(http/https)이면 그대로. null/blank → null. */
private fun absoluteUrl(value: String?): String? {
    if (value.isNullOrBlank()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    val base = RetrofitClient.baseUrl.trimEnd('/')
    val path = if (value.startsWith("/")) value else "/$value"
    return "$base$path"
}

fun ProductResponseDto.toDomain(): Product = Product(
    id = productId,
    name = productTitle,
    price = productPrice,
    imageRes = R.drawable.ic_launcher_background,  // thumbnailUrl이 null일 때 fallback
    status = parseProductStatus(productStatus),
    isLightningPay = false,
    isLiked = false,  // 찜 상태는 백엔드 구현 전까지 false 기본
    sellerId = userId,
    thumbnailUrl = absoluteUrl(thumbnailUrl),
)

fun ProductDetailResponseDto.toDomain(): ProductDetail = ProductDetail(
    id = productId,
    name = productTitle,
    price = productPrice,
    imageRes = R.drawable.ic_launcher_background,
    status = parseProductStatus(productStatus),
    isLightningPay = false,
    isLiked = false,
    description = productBody.orEmpty(),
    category = category,
    viewCount = 0,
    likeCount = 0,
    timeAgo = "",
    sellerId = userId,
    sellerNickname = sellerNickname,
    sellerRegion = sellerRegion,
    sellerAvatarRes = R.drawable.ic_launcher_foreground,
    thumbnailUrl = absoluteUrl(thumbnailUrl),
    imageUrls = imageUrls.mapNotNull { absoluteUrl(it) },
)

// ── Banner ─────────────────────────────────────────────────

fun BannerResponseDto.toDomain(): Banner = Banner(
    id = id,
    imageUrl = absoluteUrl(imageUrl).orEmpty(),
    linkUrl = linkUrl,
    title = title,
)

// ── User ───────────────────────────────────────────────────

fun UserResponseDto.toDomain(): UserProfile = UserProfile(
    id = userId,
    username = userId,
    nickname = nickname,
    email = email.orEmpty(),
    avatarUrl = null,  // 백엔드 profile_img 서빙 엔드포인트 추후 추가 시 매핑
    region = region,
    phoneNum = phoneNum,
    isAdmin = isAdmin,
)
