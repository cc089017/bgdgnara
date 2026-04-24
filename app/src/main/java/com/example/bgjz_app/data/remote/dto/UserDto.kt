package com.example.bgjz_app.data.remote.dto

import com.google.gson.annotations.SerializedName

/** GET /users/me, GET /users/{user_id} 응답 */
data class UserResponseDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("phone_num") val phoneNum: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("region") val region: String,
)

/** PATCH /users/me 요청 */
data class UserUpdateDto(
    @SerializedName("nickname") val nickname: String? = null,
    @SerializedName("phone_num") val phoneNum: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("region") val region: String? = null,
)
