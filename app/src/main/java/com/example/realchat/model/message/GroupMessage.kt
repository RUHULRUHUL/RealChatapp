package com.example.realchat.model.message

class GroupMessage (
    val date: String = "",
    val message: String = "",
    val name: String = "",
    val time: String = "",
    val type: String = "",
    val uid: String = "",


) {
    constructor() : this("", "", "", "", "", "")
}