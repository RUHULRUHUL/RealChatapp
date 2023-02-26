package com.example.realchat.repository

import androidx.lifecycle.MutableLiveData
import com.example.realchat.model.message.GroupMessage
import com.google.firebase.database.DataSnapshot

class GroupRepository {
    private val resource = ResourceOperation()
    private var groupCreateLiveData = MutableLiveData<Boolean>()
    private var sendGroupMessage = MutableLiveData<Boolean>()
    var displayMyGroups = MutableLiveData<DataSnapshot?>()

    fun groupCreate(groupName: String): MutableLiveData<Boolean> {
        groupCreateLiveData.postValue(resource.groupCreate(groupName))
        return groupCreateLiveData
    }

    fun displayMyGroups(): MutableLiveData<DataSnapshot?> {
        displayMyGroups.postValue(resource.displayMyGroups())
        return displayMyGroups
    }

    fun sendGroupMessage(groupMessage: GroupMessage,groupName: String): MutableLiveData<Boolean> {
        sendGroupMessage.postValue(resource.sendGroupMessage(groupMessage,groupName))
        return sendGroupMessage
    }

}