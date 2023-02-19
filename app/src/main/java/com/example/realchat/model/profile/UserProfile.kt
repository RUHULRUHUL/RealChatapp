package com.example.realchat.model.profile

data class UserProfile(
    val name: String = "",
    val status: String = "",
    val phone: String = "",
){
    constructor():this("","","")
}