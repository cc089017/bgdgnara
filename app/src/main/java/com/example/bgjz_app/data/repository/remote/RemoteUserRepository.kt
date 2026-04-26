package com.example.bgjz_app.data.repository.remote

import com.example.bgjz_app.data.model.ChangePasswordRequest
import com.example.bgjz_app.data.model.UpdateProfileRequest
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.remote.ApiResult
import com.example.bgjz_app.data.remote.RetrofitClient
import com.example.bgjz_app.data.remote.api.AuthApi
import com.example.bgjz_app.data.remote.api.UserApi
import com.example.bgjz_app.data.remote.dto.PasswordChangeRequestDto
import com.example.bgjz_app.data.remote.dto.UserUpdateDto
import com.example.bgjz_app.data.remote.safeApiCall
import com.example.bgjz_app.data.remote.toDomain
import com.example.bgjz_app.data.repository.UserRepository

class RemoteUserRepository(
    private val userApi: UserApi = RetrofitClient.userApi,
    private val authApi: AuthApi = RetrofitClient.authApi,
) : UserRepository {

    override suspend fun getMyProfile(): UserResult<UserProfile> {
        return when (val result = safeApiCall { userApi.getMyProfile() }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun getPublicProfile(userId: String): UserResult<UserProfile> {
        return when (val result = safeApiCall { userApi.getPublicProfile(userId) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun updateMyProfile(request: UpdateProfileRequest): UserResult<UserProfile> {
        val body = UserUpdateDto(
            nickname = request.nickname,
            region = request.region,
            phoneNum = request.phoneNum,
        )
        return when (val result = safeApiCall { userApi.updateMyProfile(body) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun uploadAvatar(imageUri: String): UserResult<String> {
        // 백엔드 아바타 업로드 엔드포인트 미구현 → Phase 1 범위 밖
        return UserResult.Error("아바타 업로드 미구현")
    }

    override suspend fun changePassword(request: ChangePasswordRequest): UserResult<Unit> {
        val body = PasswordChangeRequestDto(
            currentPwd = request.currentPassword,
            newPwd = request.newPassword,
        )
        return when (val result = safeApiCall { authApi.changePassword(body) }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun deleteAccount(): UserResult<Unit> {
        return when (val result = safeApiCall { userApi.deleteAccount() }) {
            is ApiResult.Success -> UserResult.Success(Unit)
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun getAllUsers(): UserResult<List<UserProfile>> {
        return when (val result = safeApiCall { userApi.getAllUsers() }) {
            is ApiResult.Success -> UserResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }

    override suspend fun toggleUserAdmin(userId: String): UserResult<UserProfile> {
        return when (val result = safeApiCall { userApi.toggleUserAdmin(userId) }) {
            is ApiResult.Success -> UserResult.Success(result.data.toDomain())
            is ApiResult.Error -> UserResult.Error(result.message)
        }
    }
}
