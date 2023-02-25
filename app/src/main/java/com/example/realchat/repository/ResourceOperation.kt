package com.example.realchat.repository

import android.annotation.SuppressLint
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.ActiveStatus
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.HashMap

class ResourceOperation {
    fun updateOnlineStatus(activeStatus: ActiveStatus, uid: String) {
        DBReference.userRef
            .child(uid)
            .child("UserState")
            .setValue(activeStatus)
    }

    fun onlineStatus(uid: String): String {
        var activeStatus = ""
        DBReference.userRef.child(uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("userState").hasChild("state")) {
                        val state = dataSnapshot.child("userState").child("state").value.toString()
                        if (state == "online") {
                            activeStatus = "online"

                        } else if (state == "offline") {
                            activeStatus = "offline"
                        }
                    } else {
                        activeStatus = "offline"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        return activeStatus
    }

    fun updateTypingStatus(type: KeyBordType) {
        DBReference.userRef
            .child(DBReference.uid.toString())
            .child("TypeState")
            .setValue(type)
    }

    fun profileUpdate(user: User, uid: String) {
        DBReference.userRef
            .child(uid)
            .setValue(user)
    }

    fun groupName(groupName: String, auth: FirebaseAuth) {
        DBReference.groupRef
            .child(groupName)
            .setValue("")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let {
                        DBReference.myGroupRef.child(it.uid).child(groupName)
                            .setValue("").addOnCompleteListener {
                            }
                    }
                }
            }
    }

     fun sendTxtMessage(message: Messages, senderId: String, receiverId: String) {
        val messageSenderRef = "Messages/$senderId/$receiverId"
        val messageReceiverRef = "Messages/$receiverId/$senderId"
        val messagePushID =
            DBReference.messageRef.child(senderId).child(receiverId).push().key.toString()
        val messageBodyDetails = HashMap<String, Any?>()

         message.messageID = messagePushID

        messageBodyDetails["$messageSenderRef/$messagePushID"] = message
        messageBodyDetails["$messageReceiverRef/$messagePushID"] = message
        DBReference.messageRef.updateChildren(messageBodyDetails)
    }

}