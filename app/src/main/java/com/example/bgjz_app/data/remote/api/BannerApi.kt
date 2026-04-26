package com.example.bgjz_app.data.remote.api

import com.example.bgjz_app.data.remote.dto.BannerCreateDto
import com.example.bgjz_app.data.remote.dto.BannerResponseDto
import com.example.bgjz_app.data.remote.dto.MessageResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BannerApi {

    @GET("banners")
    suspend fun getActiveBanners(): Response<List<BannerResponseDto>>

    /** 관리자 전용 */
    @POST("banners")
    suspend fun createBanner(@Body body: BannerCreateDto): Response<BannerResponseDto>

    /** 관리자 전용 */
    @DELETE("banners/{banner_id}")
    suspend fun deleteBanner(@Path("banner_id") bannerId: Int): Response<MessageResponseDto>
}
