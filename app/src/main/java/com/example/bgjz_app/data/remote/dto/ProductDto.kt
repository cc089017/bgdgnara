package com.example.bgjz_app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** PATCH /products/{id}/status 요청 바디 */
data class ProductStatusUpdateDto(
    @SerializedName("status") val status: String,
)

/** PATCH /products/{id} 요청 바디 (모든 필드 optional) */
data class ProductUpdateDto(
    @SerializedName("product_title") val productTitle: String? = null,
    @SerializedName("product_body") val productBody: String? = null,
    @SerializedName("product_price") val productPrice: Int? = null,
    @SerializedName("category") val category: String? = null,
)

/** POST /products 요청 바디 */
data class ProductCreateDto(
    @SerializedName("product_title") val productTitle: String,
    @SerializedName("product_body") val productBody: String? = null,
    @SerializedName("product_price") val productPrice: Int,
    @SerializedName("category") val category: String,
)

/** GET /products, GET /products/me, 등 공통 응답 */
data class ProductResponseDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("product_status") val productStatus: String,
    @SerializedName("product_title") val productTitle: String,
    @SerializedName("product_body") val productBody: String? = null,
    @SerializedName("product_price") val productPrice: Int,
    @SerializedName("category") val category: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
)

/** GET /products/{id} 단일 상세 응답 */
data class ProductDetailResponseDto(
    @SerializedName("product_id") val productId: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("product_status") val productStatus: String,
    @SerializedName("product_title") val productTitle: String,
    @SerializedName("product_body") val productBody: String? = null,
    @SerializedName("product_price") val productPrice: Int,
    @SerializedName("category") val category: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("seller_nickname") val sellerNickname: String,
    @SerializedName("seller_region") val sellerRegion: String,
    @SerializedName("image_urls") val imageUrls: List<String> = emptyList(),
)
