package com.example.realchatapp.model.profile

data class User(
    val name: String = "",
    val uid: String = "",
    val status: String = "",
    val phone: String = "",
    val state: String = ""
) {
    constructor() : this("", "", "", "","")
}