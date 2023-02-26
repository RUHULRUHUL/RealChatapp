package com.example.realchat.utils

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class DBReference {
    companion object {
        val uid = Firebase.auth.currentUser?.uid
        val rootRef = FirebaseDatabase.getInstance().reference
        val chatRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        val userStateRef = FirebaseDatabase.getInstance().reference.child("UserState")
        val KeyBordTypeRef = FirebaseDatabase.getInstance().reference.child("TypeState")
        val groupRef = FirebaseDatabase.getInstance().reference.child("Groups")
        val myGroupRef = FirebaseDatabase.getInstance().reference.child("MyGroupRef")
        val messageRef = FirebaseDatabase.getInstance().reference.child("Messages")


    }

}