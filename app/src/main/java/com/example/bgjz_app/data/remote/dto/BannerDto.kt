package com.example.bgjz_app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /banners 응답 항목 */
data class BannerResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("link_url") val linkUrl: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
)

/** POST /banners 요청 (관리자) */
data class BannerCreateDto(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("link_url") val linkUrl: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
)
