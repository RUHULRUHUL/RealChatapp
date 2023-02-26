package com.example.realchat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realchat.model.message.GroupMessage
import com.example.realchat.repository.GroupRepository
import com.google.firebase.database.DataSnapshot

class GroupViewModel : ViewModel() {
    private val repository = GroupRepository()
    fun groupCreate(groupName: String): MutableLiveData<Boolean> {
        return repository.groupCreate(groupName)
    }

    fun displayMyGroups(): MutableLiveData<DataSnapshot?> {
        return repository.displayMyGroups()
    }

    fun sendGroupMessage(groupMessage: GroupMessage, groupName: String): MutableLiveData<Boolean> {
        return repository.sendGroupMessage(groupMessage,groupName)
    }
}