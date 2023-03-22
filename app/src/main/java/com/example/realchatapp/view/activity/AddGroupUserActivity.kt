package com.example.realchatapp.view.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchatapp.databinding.ActivityAddGroupUserBinding
import com.example.realchatapp.helper.callBack.GroupMemberAddCallBack
import com.example.realchatapp.model.profile.User
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Validator
import com.example.realchatapp.view.adapter.GroupCreateAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddGroupUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddGroupUserBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var adapter: GroupCreateAdapter
    private lateinit var userRef: DatabaseReference
    private lateinit var selectedUserList: ArrayList<User>
    private var groupName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGroupUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initValue()
        displayUsers()

        adapter.setCallBackListener(object : GroupMemberAddCallBack {
            override fun onSelectUserList(list: ArrayList<User>) {
                selectedUserList.clear()
                if (list.size > 0) {
                    selectedUserList = list
                }
            }
        })

        binding.addUserBtn.setOnClickListener {
            Log.d("users", "userList -$selectedUserList")
            selectedUserList.forEach { user ->
                DBReference.groupRef
                    .child(groupName)
                    .child("Users").child(user.uid)
                    .setValue("")
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            DBReference.myGroupRef
                                .child(user.uid)
                                .child(groupName)
                                .setValue("")
                                .addOnCompleteListener {
                                    if (it.isSuccessful) Toast.makeText(
                                        this,
                                        "user add successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
            }
        }

    }

    private fun displayUsers() {
        val options: FirebaseRecyclerOptions<User> =
            FirebaseRecyclerOptions.Builder<User>()
                .setQuery(userRef, User::class.java)
                .build()

        adapter = GroupCreateAdapter(options, this)
        binding.userRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.userRV.adapter = adapter

    }

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        selectedUserList = ArrayList()

        groupName = intent.getStringExtra("groupName").toString().trim()

        Validator.showToast(this, groupName)

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