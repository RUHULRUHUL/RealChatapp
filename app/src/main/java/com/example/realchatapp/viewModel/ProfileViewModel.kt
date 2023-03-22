package com.example.realchatapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realchatapp.model.profile.User
import com.example.realchatapp.repository.ProfileRepository

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()
    fun profileUpdate(user: User): MutableLiveData<Boolean> {
        return repository.profileUpdate(user)
    }
}