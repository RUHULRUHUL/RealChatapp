package com.example.realchat.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.R
import com.example.realchat.databinding.ActivityGroupCreateBinding
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.view.adapter.AllUsersAdapter
import com.example.realchat.view.adapter.GroupCreateAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GroupCreateActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var adapter: GroupCreateAdapter
    private lateinit var userRef: DatabaseReference

    private lateinit var binding: ActivityGroupCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initValue()
        displayUsers()

    }

    private fun displayUsers() {
        val options: FirebaseRecyclerOptions<UserProfile> =
            FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(userRef, UserProfile::class.java)
                .build()

        adapter = GroupCreateAdapter(options, this)
        binding.findFriendRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.findFriendRV.adapter = adapter

    }

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }

}