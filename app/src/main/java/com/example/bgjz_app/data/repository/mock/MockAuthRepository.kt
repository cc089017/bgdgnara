package com.example.bgjz_app.data.repository.mock

import com.example.bgjz_app.data.model.AuthResult
import com.example.bgjz_app.data.model.AuthToken
import com.example.bgjz_app.data.model.LoginRequest
import com.example.bgjz_app.data.model.RegisterRequest
import com.example.bgjz_app.data.repository.AuthRepository
import kotlinx.coroutines.delay

class MockAuthRepository : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResult<AuthToken> {
        delay(600)
        return if (request.username.isNotBlank() && request.password.isNotBlank()) {
            AuthResult.Success(AuthToken("mock_access_token", "mock_refresh_token"))
        } else {
            AuthResult.Error("아이디 또는 비밀번호를 입력해주세요")
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResult<Unit> {
        delay(600)
        return if (listOf(request.username, request.password, request.nickname, request.email).all { it.isNotBlank() }) {
            AuthResult.Success(Unit)
        } else {
            AuthResult.Error("모든 항목을 입력해주세요")
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        delay(300)
        return AuthResult.Success(Unit)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult<AuthToken> {
        delay(300)
        return AuthResult.Success(AuthToken("mock_access_token_new", "mock_refresh_token"))
    }
}
