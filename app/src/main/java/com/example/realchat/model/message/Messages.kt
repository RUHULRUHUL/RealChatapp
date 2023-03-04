package com.example.realchat.model.message

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Messages(
    val from: String = "",
    val message: String = "",
    val type: String = "",
    val to: String = "",
    @PrimaryKey(autoGenerate = false)
    var messageID: String = "",
    val time: String = "",
    val date: String = "",
) {
    constructor() : this("", "", "", "", "", "", "")
}