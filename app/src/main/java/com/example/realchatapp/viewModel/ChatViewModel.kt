package com.example.realchatapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realchatapp.helper.room.MessageDB
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.model.profile.KeyBordType
import com.example.realchatapp.repository.ChatRepository
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

    fun getMessagesByChatUserId(messageDB:MessageDB) = chatRepository.getMessagesByChatUserId(messageDB)

}