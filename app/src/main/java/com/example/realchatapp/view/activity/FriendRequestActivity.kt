package com.example.realchatapp.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realchatapp.databinding.ActivityFriendRequestBinding
import com.example.realchatapp.databinding.UsersDisplayLayoutBinding
import com.example.realchatapp.model.profile.ActiveStatus
import com.example.realchatapp.model.profile.User
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Utils
import com.example.realchatapp.utils.Validator
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendRequestBinding
    private var chatrequestRef: DatabaseReference? = null
    private var userref: DatabaseReference? = null
    private var contactref: DatabaseReference? = null
    private var mauth: FirebaseAuth? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initValue()
    }

    private fun initValue() {
        binding.friendRequestSentRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        userref = FirebaseDatabase.getInstance().reference.child("Users")
        chatrequestRef = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        contactref = FirebaseDatabase.getInstance().reference.child("Contacts")
        mauth = FirebaseAuth.getInstance()
        currentUserId = mauth!!.currentUser!!.uid
    }

    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")
        val options: FirebaseRecyclerOptions<User> =
            FirebaseRecyclerOptions.Builder<User>()
                .setQuery(chatrequestRef!!.child(currentUserId!!), User::class.java)
                .build()
        val adapter: FirebaseRecyclerAdapter<User, RequestViewHolder> =
            object : FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: RequestViewHolder,
                    position: Int,
                    model: User
                ) {
                    holder.binding.requestAcceptButton.visibility = View.VISIBLE
                    holder.binding.requestCancelButton.visibility = View.VISIBLE

                    val userId = getRef(position).key
                    val getTypeRef = getRef(position).child("request_type").ref
                    getTypeRef.addValueEventListener(object : ValueEventListener {
                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val type = dataSnapshot.value.toString()
                                if (type == "received") {
                                    userref!!.child(userId!!)
                                        .addValueEventListener(object : ValueEventListener {
                                            @SuppressLint("SetTextI18n")
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val requestUsername =
                                                    dataSnapshot.child("name").value.toString()
                                                holder.binding.usersProfileName.text =
                                                    requestUsername
                                                holder.binding.usersStatus.text =
                                                    "Want to connect with you"
                                                holder.itemView.setOnClickListener {
                                                    val options = arrayOf<CharSequence>(
                                                        "Accept", "Cancel"
                                                    )
                                                    val builder =
                                                        AlertDialog.Builder(this@FriendRequestActivity)
                                                    builder.setTitle("$requestUsername Chat Request")
                                                    builder.setItems(
                                                        options
                                                    ) { _, which ->
                                                        if (which == 0) {
                                                            contactref!!.child(currentUserId!!)
                                                                .child(
                                                                    userId
                                                                ).child("Contacts")
                                                                .setValue("Saved")
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        contactref!!.child(userId)
                                                                            .child(
                                                                                currentUserId!!
                                                                            ).child("Contacts")
                                                                            .setValue("Saved")
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    chatrequestRef!!.child(
                                                                                        currentUserId!!
                                                                                    ).child(userId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener { task ->
                                                                                            if (task.isSuccessful) {
                                                                                                chatrequestRef!!.child(
                                                                                                    userId
                                                                                                )
                                                                                                    .child(
                                                                                                        currentUserId!!
                                                                                                    )
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener { task ->
                                                                                                        if (task.isSuccessful) {
                                                                                                            Toast.makeText(
                                                                                                                this@FriendRequestActivity,
                                                                                                                "New Contact Saved",
                                                                                                                Toast.LENGTH_SHORT
                                                                                                            )
                                                                                                                .show()
                                                                                                        }
                                                                                                    }
                                                                                            }
                                                                                        }
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                        }
                                                        if (which == 1) {
                                                            chatrequestRef!!.child(currentUserId!!)
                                                                .child(
                                                                    userId
                                                                ).removeValue()
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        chatrequestRef!!.child(
                                                                            userId
                                                                        )
                                                                            .child(
                                                                                currentUserId!!
                                                                            ).removeValue()
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    Toast.makeText(
                                                                                        this@FriendRequestActivity,
                                                                                        "Request Deleted",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                    builder.show()
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                } else if (type == "sent") {
                                    holder.binding.requestAcceptButton.text = "Cancel Req"
                                    holder.binding.requestCancelButton.visibility = View.INVISIBLE
                                    userref!!.child(userId!!)
                                        .addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val requestUsername =
                                                    dataSnapshot.child("name").value.toString()
                                                dataSnapshot.child("status").value.toString()
                                                holder.binding.usersProfileName.text =
                                                    requestUsername
                                                holder.binding.usersStatus.text =
                                                    "You have sent a request to $requestUsername"

                                                holder.itemView.setOnClickListener {
                                                    val options = arrayOf<CharSequence>(
                                                        "Cancel chat request"
                                                    )
                                                    val builder =
                                                        AlertDialog.Builder(this@FriendRequestActivity)
                                                    builder.setTitle("Already sent Request")
                                                    builder.setItems(
                                                        options
                                                    ) { _, which ->
                                                        if (which == 0) {
                                                            chatrequestRef!!.child(currentUserId!!)
                                                                .child(
                                                                    userId
                                                                ).removeValue()
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        chatrequestRef!!.child(
                                                                            userId
                                                                        )
                                                                            .child(
                                                                                currentUserId!!
                                                                            ).removeValue()
                                                                            .addOnCompleteListener { task ->
                                                                                if (task.isSuccessful) {
                                                                                    Toast.makeText(
                                                                                        this@FriendRequestActivity,
                                                                                        "You have cancelled the chat request.",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                        }
                                                    }
                                                    builder.show()
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {}
                                        })
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RequestViewHolder {

                    return RequestViewHolder(
                        UsersDisplayLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )

                }
            }
        binding.friendRequestSentRV.adapter = adapter
        adapter.startListening()
    }

    class RequestViewHolder(val binding: UsersDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onPause() {
        super.onPause()
        userStatusUpdate("offline")
    }

    override fun onResume() {
        super.onResume()
        userStatusUpdate("online")

    }

    override fun onRestart() {
        super.onRestart()
        userStatusUpdate("online")

    }

    override fun onStop() {
        super.onStop()
        userStatusUpdate("offline")
    }

    override fun onDestroy() {
        super.onDestroy()
        userStatusUpdate("offline")
    }

    private fun userStatusUpdate(state: String) {
        if (Utils.isNetworkAvailable(this)) {
            val activeStatus = ActiveStatus(
                state,
                Validator.getCurrentDate(),
                Validator.getCurrentTime()
            )
            DBReference.userRef
                .child(mauth?.uid.toString())
                .child("UserState")
                .setValue(activeStatus)
        }
    }
}