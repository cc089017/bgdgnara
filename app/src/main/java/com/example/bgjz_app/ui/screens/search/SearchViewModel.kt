package com.example.bgjz_app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bgjz_app.data.mock.Product
import com.example.bgjz_app.data.model.UserResult
import com.example.bgjz_app.data.repository.mock.MockProductRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    // 백엔드 연결 시: MockProductRepository() → RemoteProductRepository(retrofit)
    private val repository = MockProductRepository()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query -> doSearch(query) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        _queryFlow.value = query
    }

    private suspend fun doSearch(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false, isEmpty = false)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true)
        when (val result = repository.searchProducts(query)) {
            is UserResult.Success -> _uiState.value = _uiState.value.copy(
                results = result.data,
                isLoading = false,
                isEmpty = result.data.isEmpty()
            )
            is UserResult.Error -> _uiState.value = _uiState.value.copy(
                results = emptyList(),
                isLoading = false,
                isEmpty = true
            )
        }
    }
}
