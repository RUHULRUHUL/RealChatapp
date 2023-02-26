package com.example.realchat.model.message

data class GroupMessage (
    val date: String = "",
    val message: String = "",
    var messageId: String = "",
    val name: String = "",
    val time: String = "",
    val type: String = "",
    val uid: String = "",
) {
    constructor() : this("", "", "", "", "", "","")
}