package com.example.realchatapp.repository

import androidx.lifecycle.MutableLiveData
import com.example.realchatapp.helper.room.MessageDB
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.model.profile.KeyBordType

class ChatRepository {
    private val resource = ResourceOperation()
    var onlineStatusLiveData = MutableLiveData<String>()

    fun updateTypingStatus(type: KeyBordType) {
        resource.updateTypingStatus(type)
    }

    fun sendMessage(messageDB:MessageDB,messages: Messages, receiverId: String) {
        resource.sendTxtMessage(messageDB,messages, receiverId)
    }

    fun getOnlineStatus(receiverId: String):MutableLiveData<String>{
        onlineStatusLiveData.postValue(resource.getOnlineStatus(receiverId))
        return onlineStatusLiveData
    }

    fun getMessagesByChatUserId(messageDB:MessageDB) = messageDB.messageDao()?.getAllMessages()


}