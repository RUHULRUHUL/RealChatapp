package com.example.realchat.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.databinding.ActivityGroupCreateBinding
import com.example.realchat.model.message.GroupMessage
import com.example.realchat.utils.DBReference
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.GroupMessageAdapter
import com.example.realchat.viewModel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupCreateBinding
    private lateinit var groupViewModel: GroupViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var adapter: GroupMessageAdapter
    private val messagesList = ArrayList<GroupMessage>()
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var currentUserName = ""
    private var groupName = ""
    private var prevKey = ""
    private var lastKey = ""
    private var itemPosition = 0
    private var topMessageKey = ""
    private var isLoading: Boolean = false
    private var isFirstTimeLoad = false
    private var loadMorePageStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
        getUserInfo()
        getTopKey()
        loadMessage()
        clickEvent()
    }

    private fun getTopKey() {
        val query = DBReference.groupRef
            .child(intent.getStringExtra("groupName").toString())
            .orderByKey()
            .limitToFirst(1)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val message = item.getValue(GroupMessage::class.java)
                    topMessageKey = message?.messageId.toString()
                    Log.d("TopKey", "Top key $topMessageKey")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun loadMessage() {
        itemPosition = 0
        isLoading = true
        binding.loader.visibility = View.VISIBLE
        val query = DBReference.groupRef
            .child(intent.getStringExtra("groupName").toString())
            .orderByKey()
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                binding.loader.visibility = View.GONE
                isLoading = false
                if (dataSnapshot.exists()) {
                    val message = dataSnapshot.getValue(GroupMessage::class.java)
                    message?.let {
                        if (!loadMorePageStatus) {
                            Validator.showToast(this@GroupChatActivity,"last KEy")
                            itemPosition++
                            if (itemPosition == 1) {
                                lastKey = dataSnapshot.key.toString()
                                prevKey = dataSnapshot.key.toString()
                            }
                            messagesList.add(message)
                            adapter.notifyDataSetChanged()
                            binding.privateMessageListOfUsers.scrollToPosition(messagesList.size - 1)
                        } else {
                            Validator.showToast(this@GroupChatActivity,"prev last KEy")
                            messagesList.add(message)
                            adapter.notifyDataSetChanged()
                            binding.privateMessageListOfUsers.scrollToPosition(messagesList.size - 1)
                        }
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun loadMoreMessage() {
        itemPosition = 0
        prevKey = lastKey
        val query = DBReference.groupRef
            .child(intent.getStringExtra("groupName").toString())
            .orderByKey()
            .endAt(lastKey)
            .limitToLast(10)

        query.addChildEventListener(object : ChildEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                binding.loader.visibility = View.GONE
                if (dataSnapshot.exists()) {
                    loadMorePageStatus = true
                    isLoading = false

                    val messages = dataSnapshot.getValue(GroupMessage::class.java)
                    messages?.let {
                        if (prevKey != dataSnapshot.key) {
                            messagesList.add(itemPosition++, messages)
                            Log.d("GroupChatMSG", "loadMoreMessage: " + messages.message)
                        }
                        if (itemPosition == 1) {
                            lastKey = dataSnapshot.key.toString()
                        }

                        adapter.notifyDataSetChanged()
                        linearLayoutManager.scrollToPositionWithOffset(8, 0)
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun clickEvent() {
        binding.privateMessageListOfUsers.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isLoading) {
                        if (lastKey == topMessageKey) {
                            Log.d(
                                "topLastMessageKey",
                                "rich to top no more data lastKEy $lastKey topKey: $topMessageKey are the same"
                            )
                            Validator.showToast(this@GroupChatActivity, "No More Data")
                        } else {
                            binding.loader.visibility = View.VISIBLE
                            isLoading = true
                            object : CountDownTimer(2000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                }

                                override fun onFinish() {
                                    loadMoreMessage()
                                }
                            }.start()
                        }
                    } else {
                        Validator.showToast(this@GroupChatActivity, "Please wait for loading...")
                    }
                }
            }
        })

        binding.inputMessages.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_DOWN == event.action) {
                if (messagesList.size > 0) {
                    binding.privateMessageListOfUsers.scrollToPosition(messagesList.size - 1)
                }
            }
            false
        }

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

        adapter = GroupMessageAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessageListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessageListOfUsers.adapter = adapter
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