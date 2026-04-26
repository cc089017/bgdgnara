package com.example.bgjz_app.data.repository

import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.UserResult

interface BannerRepository {
    suspend fun getActiveBanners(): UserResult<List<Banner>>

    // ── 관리자 전용 ──
    suspend fun createBanner(
        imageUrl: String,
        title: String? = null,
        linkUrl: String? = null,
        isActive: Boolean = true,
    ): UserResult<Banner>

    suspend fun deleteBanner(bannerId: Int): UserResult<Unit>
}
