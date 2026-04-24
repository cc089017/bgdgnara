package com.example.bgjz_app.ui.screens.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.UserProfile
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.ProductRepository
import com.example.bgjz_app.data.repository.UserRepository
import com.example.bgjz_app.data.repository.remote.RemoteProductRepository
import com.example.bgjz_app.data.repository.remote.RemoteUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

class UserProfileViewModel(
    private val userRepository: UserRepository = RemoteUserRepository(),
    private val productRepository: ProductRepository = RemoteProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val profileResult = userRepository.getPublicProfile(userId)
            val productsResult = productRepository.getProductsByUser(userId)

            val profile = when (profileResult) {
                is UserResult.Success -> profileResult.data
                is UserResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = profileResult.message) }
                    return@launch
                }
            }
            val products = when (productsResult) {
                is UserResult.Success -> productsResult.data
                is UserResult.Error -> emptyList()
            }

            _uiState.update { it.copy(isLoading = false, profile = profile, products = products) }
        }
    }
}
