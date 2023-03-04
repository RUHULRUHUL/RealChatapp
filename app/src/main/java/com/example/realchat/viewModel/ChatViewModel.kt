package com.example.realchat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realchat.helper.room.MessageDB
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.repository.ChatRepository
class ChatViewModel:ViewModel() {
    private val chatRepository = ChatRepository()
    fun updateTypingStatus(type:KeyBordType){
        chatRepository.updateTypingStatus(type)
    }
    fun sendMessage(messageDB:MessageDB,messages: Messages, receiverId: String){
        chatRepository.sendMessage(messageDB,messages, receiverId)
    }

    fun getOnlineStatus(receiverId: String):MutableLiveData<String>{
        return chatRepository.getOnlineStatus(receiverId)
    }

}