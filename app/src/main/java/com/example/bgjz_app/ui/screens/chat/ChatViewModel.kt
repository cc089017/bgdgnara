package com.example.bgjz_app.ui.screens.chat

import androidx.lifecycle.ViewModel
import com.example.bgjz_app.data.mock.ChatMessage
import com.example.bgjz_app.data.mock.ChatRoom
import com.example.bgjz_app.data.mock.MockData

class ChatViewModel : ViewModel() {
    val chatRooms: List<ChatRoom> = MockData.chatRooms

    fun lastMessage(roomId: Int): ChatMessage? =
        MockData.chatMessages[roomId]?.lastOrNull()
}
