package com.example.realchat.view.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.ActivityGroupCreateBinding
import com.example.realchat.model.message.GroupMessage
import com.example.realchat.utils.DBReference
import com.example.realchat.view.adapter.GroupMessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class GroupCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupCreateBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private var currentUserName = ""
    private lateinit var adapter: GroupMessageAdapter
    private val messagesList = ArrayList<GroupMessage>()
    private lateinit var userRef: DatabaseReference
    private lateinit var groupRef: DatabaseReference


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
                sendGroupMessage()
            }
        }

        binding.addUsers.setOnClickListener {
            startActivity(Intent(this, AddGroupUserActivity::class.java))
        }
    }

/*    private fun displayUsers() {
        val options: FirebaseRecyclerOptions<UserProfile> =
            FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(userRef, UserProfile::class.java)
                .build()

        adapter = GroupCreateAdapter(options, this)
        binding.findFriendRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.findFriendRV.adapter = adapter

    }*/

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        groupRef = FirebaseDatabase.getInstance().reference.child("Groups")
            .child(intent.getStringExtra("groupName").toString().trim())

        binding.customProfileName.text = intent.getStringExtra("groupName")

    }

    override fun onStart() {
        super.onStart()
        //adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        // adapter.stopListening()
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

    private fun sendGroupMessage() {
        val message = binding.inputMessages.text.toString()
        val messageKey = groupRef.push().key
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show()
        } else {
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MMM dd, yyyy")
            val currentDate = currentDateFormat.format(calForDate.time)
            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a")
            val currentTime = currentTimeFormat.format(calForTime.time)

            val groupMessageKey = HashMap<String, Any>()
            DBReference.groupRef.updateChildren(groupMessageKey)

            val messageInfoMap = HashMap<String, Any>()
            messageInfoMap["name"] = currentUserName
            messageInfoMap["message"] = message
            messageInfoMap["date"] = currentDate
            messageInfoMap["time"] = currentTime
            messageInfoMap["type"] = "text"
            messageInfoMap["uid"] = mAuth.currentUser?.uid.toString()

            messageKey?.let {
                groupRef.child(it)
            }?.updateChildren(messageInfoMap)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    binding.inputMessages.setText("")
                }
            }
        }
    }

}