package com.example.bgjz_app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bgjz_app.data.mock.ChatMessage
import com.example.bgjz_app.data.mock.ChatRoom
import com.example.bgjz_app.data.mock.MockData
import com.example.bgjz_app.data.mock.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRoomViewModel(private val chatRoomId: Int) : ViewModel() {

    // 백엔드 연결 시: GET /chats/{chatRoomId}/messages 로 교체
    val room: ChatRoom? = MockData.chatRooms.find { it.id == chatRoomId }
    val product: Product? = room?.let { r -> MockData.products.find { it.id == r.productId } }

    private val _messages = MutableStateFlow(
        MockData.chatMessages[chatRoomId] ?: emptyList()
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val newMsg = ChatMessage(
            id = (_messages.value.maxOfOrNull { it.id } ?: 0) + 1,
            senderId = "me",
            content = content.trim(),
            timestamp = "방금"
        )
        _messages.value = _messages.value + newMsg
    }

    companion object {
        fun factory(chatRoomId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChatRoomViewModel(chatRoomId) as T
        }
    }
}
