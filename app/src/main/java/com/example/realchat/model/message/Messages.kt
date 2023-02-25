package com.example.realchat.model.message

data class Messages(
     val from: String = "",
     val message: String = "",
     val type: String = "",
     val to: String = "",
     var messageID: String = "",
     val time: String = "",
     val date: String = "",
     val name: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "")
}