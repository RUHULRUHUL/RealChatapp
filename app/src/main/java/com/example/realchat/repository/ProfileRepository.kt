package com.example.realchat.repository

import androidx.lifecycle.MutableLiveData
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference

class ProfileRepository {
    private val resource = ResourceOperation()
    var profileUpdateLiveData = MutableLiveData<Boolean>()

    fun profileUpdate(user: User): MutableLiveData<Boolean> {
        profileUpdateLiveData.postValue(resource.profileUpdate(user, DBReference.uid.toString()))
        return profileUpdateLiveData
    }
}