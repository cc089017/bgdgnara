package com.example.bgjz_app.data.repository

import com.example.bgjz_app.data.model.ChangePasswordRequest
import com.example.bgjz_app.data.model.UpdateProfileRequest
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult

interface UserRepository {
    suspend fun getMyProfile(): UserResult<UserProfile>
    suspend fun getPublicProfile(userId: String): UserResult<UserProfile>
    suspend fun updateMyProfile(request: UpdateProfileRequest): UserResult<UserProfile>
    suspend fun uploadAvatar(imageUri: String): UserResult<String>
    suspend fun changePassword(request: ChangePasswordRequest): UserResult<Unit>
    suspend fun deleteAccount(): UserResult<Unit>
}
