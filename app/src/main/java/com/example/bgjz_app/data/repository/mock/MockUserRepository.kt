package com.example.bgjz_app.data.repository.mock

import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Seller
import com.example.bgjz_app.data.model.ChangePasswordRequest
import com.example.bgjz_app.data.model.UpdateProfileRequest
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.UserRepository
import kotlinx.coroutines.delay

class MockUserRepository : UserRepository {

    private var mockProfile = UserProfile(
        id = "bgjz_user",
        username = "bgjz_user",
        nickname = MockData.currentUser.nickname,
        email = MockData.currentUser.email,
        avatarUrl = null,
        region = "서울 강남구"
    )

    override suspend fun getPublicProfile(userId: String): UserResult<UserProfile> {
        delay(400)
        val seller = MockData.sellers.find { it.id == userId }
            ?: return UserResult.Error("유저를 찾을 수 없습니다")
        return UserResult.Success(
            UserProfile(
                id = seller.id,
                username = seller.nickname,
                nickname = seller.nickname,
                email = "",
                avatarUrl = null,
                region = seller.region
            )
        )
    }

    override suspend fun getMyProfile(): UserResult<UserProfile> {
        delay(400)
        return UserResult.Success(mockProfile)
    }

    override suspend fun updateMyProfile(request: UpdateProfileRequest): UserResult<UserProfile> {
        delay(600)
        mockProfile = mockProfile.copy(nickname = request.nickname, region = request.region)
        return UserResult.Success(mockProfile)
    }

    override suspend fun uploadAvatar(imageUri: String): UserResult<String> {
        delay(800)
        return UserResult.Success(imageUri)
    }

    override suspend fun changePassword(request: ChangePasswordRequest): UserResult<Unit> {
        delay(600)
        return if (request.currentPassword.isNotBlank() && request.newPassword.isNotBlank()) {
            UserResult.Success(Unit)
        } else {
            UserResult.Error("비밀번호를 입력해주세요")
        }
    }

    override suspend fun deleteAccount(): UserResult<Unit> {
        delay(600)
        return UserResult.Success(Unit)
    }

    override suspend fun getAllUsers(): UserResult<List<UserProfile>> =
        UserResult.Error("Mock: 관리자 기능 미구현")

    override suspend fun toggleUserAdmin(userId: String): UserResult<UserProfile> =
        UserResult.Error("Mock: 관리자 기능 미구현")
}
