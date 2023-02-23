package com.example.realchat.model.profile

data class User(
    val name: String = "",
    val uid: String = "",
    val status: String = "",
    val phone: String = "",
    val state: String = ""
) {
    constructor() : this("", "", "", "","")
}