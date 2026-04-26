package com.example.bgjz_app.data.remote.api

import com.example.bgjz_app.data.remote.dto.MessageResponseDto
import com.example.bgjz_app.data.remote.dto.ProductResponseDto
import com.example.bgjz_app.data.remote.dto.UserResponseDto
import com.example.bgjz_app.data.remote.dto.UserUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserApi {

    @GET("users/me")
    suspend fun getMyProfile(): Response<UserResponseDto>

    @PATCH("users/me")
    suspend fun updateMyProfile(@Body body: UserUpdateDto): Response<UserResponseDto>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<MessageResponseDto>

    @GET("users/{user_id}")
    suspend fun getPublicProfile(@Path("user_id") userId: String): Response<UserResponseDto>

    @GET("users/{user_id}/products")
    suspend fun getUserProducts(@Path("user_id") userId: String): Response<List<ProductResponseDto>>

    /** 관리자 전용: 전체 유저 목록 */
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponseDto>>

    /** 관리자 전용: 관리자 권한 토글 */
    @PATCH("users/{user_id}/admin")
    suspend fun toggleUserAdmin(@Path("user_id") userId: String): Response<UserResponseDto>
}
