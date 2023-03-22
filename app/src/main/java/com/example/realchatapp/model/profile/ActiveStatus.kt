package com.example.realchatapp.model.profile

class ActiveStatus(
    val state: String = "",
    val date: String = "",
    val time: String = "",
) {
    constructor() : this("", "", "")
}