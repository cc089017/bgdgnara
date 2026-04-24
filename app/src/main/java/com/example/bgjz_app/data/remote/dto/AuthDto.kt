package com.example.bgjz_app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** POST /auth/register 요청 */
data class RegisterRequestDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_pwd") val userPwd: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("phone_num") val phoneNum: String,
    @SerializedName("email") val email: String?,
    @SerializedName("region") val region: String,
)

/** POST /auth/register 응답 */
data class RegisterResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: String,
)

/** POST /auth/login 요청 */
data class LoginRequestDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_pwd") val userPwd: String,
)

/** POST /auth/login 응답 */
data class LoginResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
)

/** POST /auth/logout 요청 */
data class LogoutRequestDto(
    @SerializedName("refresh_token") val refreshToken: String,
)

/** POST /auth/refresh 요청 */
data class RefreshRequestDto(
    @SerializedName("refresh_token") val refreshToken: String,
)

/** POST /auth/refresh 응답 */
data class RefreshResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
)

/** PUT /auth/password/change 요청 */
data class PasswordChangeRequestDto(
    @SerializedName("current_pwd") val currentPwd: String,
    @SerializedName("new_pwd") val newPwd: String,
)

/** 공통 메시지 응답 */
data class MessageResponseDto(
    @SerializedName("message") val message: String,
)
