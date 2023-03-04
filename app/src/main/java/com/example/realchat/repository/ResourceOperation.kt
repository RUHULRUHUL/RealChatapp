package com.example.realchat.repository

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.realchat.helper.room.MessageDB
import com.example.realchat.model.message.GroupMessage
import com.example.realchat.model.message.Messages
import com.example.realchat.model.profile.ActiveStatus
import com.example.realchat.model.profile.KeyBordType
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ResourceOperation {


    fun updateOnlineStatus(activeStatus: ActiveStatus, uid: String) {
        DBReference.userRef
            .child(uid)
            .child("UserState")
            .setValue(activeStatus)
    }

    fun getOnlineStatus(receiverId: String): String {
        var activeStatus = ""
        DBReference.userRef.child(receiverId)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child("UserState").hasChild("state")) {
                        val state = dataSnapshot.child("UserState").child("state").value.toString()
                        Log.d("getOnlineStatus", "online Status: $state")
                        activeStatus = state
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

    fun profileUpdate(user: User, uid: String): Boolean {
        var profileUpdateStatus = false
        DBReference.userRef
            .child(uid)
            .setValue(user)
            .addOnCompleteListener {
                profileUpdateStatus = it.isSuccessful
            }
        return profileUpdateStatus
    }

    fun groupCreate(groupName: String): Boolean {
        var groupCreateStatus = false
        DBReference.groupRef
            .child(groupName)
            .setValue("")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    DBReference.uid?.let { it ->
                        DBReference.myGroupRef.child(it).child(groupName)
                            .setValue("").addOnCompleteListener {
                                groupCreateStatus = it.isSuccessful
                            }
                    }
                }
            }
        return groupCreateStatus
    }

    @SuppressLint("SuspiciousIndentation")
    fun displayMyGroups(): DataSnapshot? {
        var snapshot: DataSnapshot? = null
        DBReference.myGroupRef
            .child(DBReference.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    snapshot = dataSnapshot
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        return snapshot
    }

    fun sendGroupMessage(groupMessage: GroupMessage, groupName: String): Boolean {
        var groupCreateStatus = false
        val messageKey = DBReference.groupRef.child(groupName).push().key
        groupMessage.messageId = messageKey.toString()
        val messageBodyDetails = HashMap<String, Any?>()
        messageBodyDetails["Groups/$groupName/$messageKey"] = groupMessage
        DBReference.rootRef.updateChildren(messageBodyDetails)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    groupCreateStatus = true
                }
            }
        return groupCreateStatus
    }

    fun sendTxtMessage(messageDB: MessageDB, message: Messages, receiverId: String) {
        val senderId = DBReference.uid.toString()
        val messageSenderRef = "Messages/$senderId/$receiverId"
        val messageReceiverRef = "Messages/$receiverId/$senderId"
        val messagePushID =
            DBReference.messageRef.child(senderId).child(receiverId).push().key.toString()
        val messageBodyDetails = HashMap<String, Any?>()
        message.messageID = messagePushID
        messageBodyDetails["$messageSenderRef/$messagePushID"] = message
        messageBodyDetails["$messageReceiverRef/$messagePushID"] = message
        DBReference.rootRef.updateChildren(messageBodyDetails)
            .addOnCompleteListener {
                CoroutineScope(Dispatchers.IO).launch {
                    messageDB.messageDao()?.insertMessage(message)
                }
            }
    }

}