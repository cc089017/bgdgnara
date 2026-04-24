package com.example.bgjz_app.data.repository.remote

import com.example.bgjz_app.data.model.AuthResult
import com.example.bgjz_app.data.model.AuthToken
import com.example.bgjz_app.data.model.LoginRequest
import com.example.bgjz_app.data.model.RegisterRequest
import com.example.bgjz_app.data.remote.ApiResult
import com.example.bgjz_app.data.remote.RetrofitClient
import com.example.bgjz_app.data.remote.TokenStorage
import com.example.bgjz_app.data.remote.api.AuthApi
import com.example.bgjz_app.data.remote.dto.LoginRequestDto
import com.example.bgjz_app.data.remote.dto.LogoutRequestDto
import com.example.bgjz_app.data.remote.dto.RefreshRequestDto
import com.example.bgjz_app.data.remote.dto.RegisterRequestDto
import com.example.bgjz_app.data.remote.safeApiCall
import com.example.bgjz_app.data.repository.AuthRepository

class RemoteAuthRepository(
    private val api: AuthApi = RetrofitClient.authApi,
    private val tokenStorage: TokenStorage = RetrofitClient.tokenStorage,
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResult<AuthToken> {
        val result = safeApiCall {
            api.login(LoginRequestDto(userId = request.username, userPwd = request.password))
        }
        return when (result) {
            is ApiResult.Success -> {
                tokenStorage.saveTokens(result.data.accessToken, result.data.refreshToken)
                AuthResult.Success(AuthToken(result.data.accessToken, result.data.refreshToken))
            }
            is ApiResult.Error -> AuthResult.Error(result.message)
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResult<Unit> {
        val result = safeApiCall {
            api.register(
                RegisterRequestDto(
                    userId = request.username,
                    userPwd = request.password,
                    nickname = request.nickname,
                    phoneNum = request.phoneNum,
                    email = request.email.ifBlank { null },
                    region = request.region,
                )
            )
        }
        return when (result) {
            is ApiResult.Success -> AuthResult.Success(Unit)
            is ApiResult.Error -> AuthResult.Error(result.message)
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: run {
                tokenStorage.clearTokens()
                return AuthResult.Success(Unit)
            }
        val result = safeApiCall { api.logout(LogoutRequestDto(refreshToken)) }
        tokenStorage.clearTokens()
        return when (result) {
            is ApiResult.Success -> AuthResult.Success(Unit)
            is ApiResult.Error -> AuthResult.Success(Unit)  // 서버 거절되어도 로컬은 비움
        }
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult<AuthToken> {
        val result = safeApiCall { api.refresh(RefreshRequestDto(refreshToken)) }
        return when (result) {
            is ApiResult.Success -> {
                tokenStorage.saveTokens(result.data.accessToken, refreshToken)
                AuthResult.Success(AuthToken(result.data.accessToken, refreshToken))
            }
            is ApiResult.Error -> AuthResult.Error(result.message)
        }
    }
}
