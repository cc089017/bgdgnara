package com.example.bgjz_app.data.remote.api

import com.example.bgjz_app.data.remote.dto.LoginRequestDto
import com.example.bgjz_app.data.remote.dto.LoginResponseDto
import com.example.bgjz_app.data.remote.dto.LogoutRequestDto
import com.example.bgjz_app.data.remote.dto.MessageResponseDto
import com.example.bgjz_app.data.remote.dto.PasswordChangeRequestDto
import com.example.bgjz_app.data.remote.dto.RefreshRequestDto
import com.example.bgjz_app.data.remote.dto.RefreshResponseDto
import com.example.bgjz_app.data.remote.dto.RegisterRequestDto
import com.example.bgjz_app.data.remote.dto.RegisterResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<RegisterResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<MessageResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<RefreshResponseDto>

    @PUT("auth/password/change")
    suspend fun changePassword(@Body body: PasswordChangeRequestDto): Response<MessageResponseDto>
}
