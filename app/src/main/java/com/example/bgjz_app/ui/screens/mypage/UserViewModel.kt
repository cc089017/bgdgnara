package com.example.bgjz_app.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.model.ChangePasswordRequest
import com.example.bgjz_app.data.model.UpdateProfileRequest
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.UserRepository
import com.example.bgjz_app.data.repository.mock.MockUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false,
    val isPasswordChangeSuccess: Boolean = false,
    val isDeleteSuccess: Boolean = false
)

// 백엔드 연결 시: MockUserRepository() → RemoteUserRepository(retrofit)
class UserViewModel(
    private val repository: UserRepository = MockUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadMyProfile()
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getMyProfile()) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, profile = result.data) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun updateProfile(nickname: String, region: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateMyProfile(UpdateProfileRequest(nickname, region.ifBlank { null }))) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, profile = result.data, isUpdateSuccess = true) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun uploadAvatar(imageUri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.uploadAvatar(imageUri)) {
                is UserResult.Success -> _uiState.update {
                    it.copy(isLoading = false, profile = it.profile?.copy(avatarUrl = result.data))
                }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.changePassword(ChangePasswordRequest(currentPassword, newPassword))) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, isPasswordChangeSuccess = true) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.deleteAccount()) {
                is UserResult.Success -> _uiState.update { it.copy(isLoading = false, isDeleteSuccess = true) }
                is UserResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(isUpdateSuccess = false, isPasswordChangeSuccess = false, isDeleteSuccess = false, error = null) }
    }
}
