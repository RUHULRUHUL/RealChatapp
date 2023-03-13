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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupMessage

        if (date != other.date) return false
        if (message != other.message) return false
        if (messageId != other.messageId) return false
        if (name != other.name) return false
        if (time != other.time) return false
        if (type != other.type) return false
        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + uid.hashCode()
        return result
    }
}