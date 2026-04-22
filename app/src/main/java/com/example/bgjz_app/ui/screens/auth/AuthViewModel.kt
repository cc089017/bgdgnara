package com.example.bgjz_app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.model.AuthResult
import com.example.bgjz_app.data.model.LoginRequest
import com.example.bgjz_app.data.model.RegisterRequest
import com.example.bgjz_app.data.repository.AuthRepository
import com.example.bgjz_app.data.repository.mock.MockAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false
)

// 백엔드 연결 시: MockAuthRepository() → RemoteAuthRepository(retrofit)
class AuthViewModel(
    private val repository: AuthRepository = MockAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.login(LoginRequest(username, password))) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun register(username: String, password: String, nickname: String, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.register(RegisterRequest(username, password, nickname, email))) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
