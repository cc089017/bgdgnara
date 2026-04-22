package com.example.bgjz_app.data.repository

import com.example.bgjz_app.data.model.AuthResult
import com.example.bgjz_app.data.model.AuthToken
import com.example.bgjz_app.data.model.LoginRequest
import com.example.bgjz_app.data.model.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResult<AuthToken>
    suspend fun register(request: RegisterRequest): AuthResult<Unit>
    suspend fun logout(): AuthResult<Unit>
    suspend fun refreshToken(refreshToken: String): AuthResult<AuthToken>
}
