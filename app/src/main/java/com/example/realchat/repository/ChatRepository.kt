package com.example.realchat.repository

import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.KeyBordType

class ChatRepository {
    private val resource = ResourceOperation()

    fun updateTypingStatus(type: KeyBordType) {
        resource.updateTypingStatus(type)
    }

    fun sendMessage(messages: Messages, senderId: String, receiverId: String) {
        resource.sendTxtMessage(messages, senderId, receiverId)
    }


}