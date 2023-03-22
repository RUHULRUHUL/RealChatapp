package com.example.realchatapp.repository

import androidx.lifecycle.MutableLiveData
import com.example.realchatapp.model.profile.User
import com.example.realchatapp.utils.DBReference

class ProfileRepository {
    private val resource = ResourceOperation()
    var profileUpdateLiveData = MutableLiveData<Boolean>()

    fun profileUpdate(user: User): MutableLiveData<Boolean> {
        profileUpdateLiveData.postValue(resource.profileUpdate(user, DBReference.uid.toString()))
        return profileUpdateLiveData
    }
}