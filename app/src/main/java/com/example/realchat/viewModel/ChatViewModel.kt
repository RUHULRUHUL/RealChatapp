package com.example.realchat.viewModel

import androidx.lifecycle.ViewModel
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.repository.ChatRepository
class ChatViewModel:ViewModel() {

    private val chatRepository = ChatRepository()

    fun updateTypingStatus(type:KeyBordType){
        chatRepository.updateTypingStatus(type)
    }

    fun sendMessage(messages: Messages, senderId: String, receiverId: String){
        chatRepository.sendMessage(messages, senderId, receiverId)
    }

}