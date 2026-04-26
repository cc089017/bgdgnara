package com.example.bgjz_app.data.repository.remote

import com.example.bgjz_app.data.model.Banner
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.remote.ApiResult
import com.example.bgjz_app.data.remote.RetrofitClient
import com.example.bgjz_app.data.remote.api.BannerApi
import com.example.bgjz_app.data.remote.dto.BannerCreateDto
import com.example.bgjz_app.data.remote.safeApiCall
import com.example.bgjz_app.data.remote.toDomain
import com.example.bgjz_app.data.repository.BannerRepository

class RemoteBannerRepository(
    private val bannerApi: BannerApi = RetrofitClient.bannerApi,
) : BannerRepository {

    override suspend fun getActiveBanners(): UserResult<List<Banner>> {
        return when (val result = safeApiCall { bannerApi.getActiveBanners() }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun createBanner(
        imageUrl: String,
        title: String?,
        linkUrl: String?,
        isActive: Boolean,
    ): UserResult<Banner> {
        val body = BannerCreateDto(
            imageUrl = imageUrl,
            title = title,
            linkUrl = linkUrl,
            isActive = isActive,
        )
        return when (val result = safeApiCall { bannerApi.createBanner(body) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun deleteBanner(bannerId: Int): UserResult<Unit> {
        return when (val result = safeApiCall { bannerApi.deleteBanner(bannerId) }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }
}
