package com.example.realchat.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realchat.model.profile.User
import com.example.realchat.repository.ProfileRepository

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()
    fun profileUpdate(user: User): MutableLiveData<Boolean> {
        return repository.profileUpdate(user)
    }
}