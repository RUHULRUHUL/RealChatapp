package com.example.realchat.model.profile

class ActiveStatus(
    val state: String = "",
    val date: String = "",
    val time: String = "",
) {
    constructor() : this("", "", "")
}