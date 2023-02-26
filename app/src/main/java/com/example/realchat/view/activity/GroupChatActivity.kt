package com.example.realchat.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.ActivityGroupCreateBinding
import com.example.realchat.model.message.GroupMessage
import com.example.realchat.utils.DBReference
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.GroupMessageAdapter
import com.example.realchat.viewModel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupCreateBinding
    private lateinit var groupViewModel: GroupViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: GroupMessageAdapter
    private val messagesList = ArrayList<GroupMessage>()
    private var currentUserName = ""
    private var groupName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        getUserInfo()
        getDisplayMessage()
        realtimeFetchMsg()
        clickEvent()
    }

    private fun realtimeFetchMsg() {
        DBReference.groupRef.child(intent.getStringExtra("groupName").toString())
            .addChildEventListener(object : ChildEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val groupMessage = dataSnapshot.getValue(GroupMessage::class.java)
                    if (groupMessage != null) {
                        messagesList.add(groupMessage)
                    }
                    adapter.notifyDataSetChanged()
                    binding.privateMessageListOfUsers.smoothScrollToPosition(binding.privateMessageListOfUsers.adapter!!.itemCount)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun getDisplayMessage() {
        adapter = GroupMessageAdapter(messagesList)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessageListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessageListOfUsers.adapter = adapter
    }

    private fun clickEvent() {
        binding.sendMessageBtn.setOnClickListener {
            mAuth.currentUser?.let {
                sendGroupTxtMessage()
            }
        }

        binding.addUsers.setOnClickListener {
            val intent = Intent(this, AddGroupUserActivity::class.java)
            intent.putExtra("groupName", groupName)
            startActivity(intent)
        }
    }

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        binding.customProfileName.text = intent.getStringExtra("groupName")
        groupName = binding.customProfileName.text.toString().trim()
        groupViewModel = ViewModelProvider(this)[GroupViewModel::class.java]
    }

    private fun getUserInfo() {
        mAuth.currentUser?.let {
            DBReference.userRef.child(it.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        currentUserName = dataSnapshot.child("name").value.toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    private fun sendGroupTxtMessage() {
        val message = GroupMessage(
            Validator.getCurrentDate(),
            binding.inputMessages.text.toString(),
            "",
            "",
            Validator.getCurrentTime(),
            "text",
            mAuth.uid.toString()
        )
        groupViewModel.sendGroupMessage(message, groupName)
            .observe(this) {
                if (it) {
                    binding.inputMessages.setText("")
                }
            }
        binding.inputMessages.setText("")
    }

}