package com.example.bgjz_app.data.model

data class UserProfile(
    val id: String,
    val username: String,
    val nickname: String,
    val email: String,
    val avatarUrl: String?,
    val region: String?,
    val phoneNum: String? = null,
)

data class UpdateProfileRequest(
    val nickname: String,
    val region: String?,
    val phoneNum: String? = null,
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

sealed class UserResult<out T> {
    data class Success<T>(val data: T) : UserResult<T>()
    data class Error(val message: String) : UserResult<Nothing>()
}
