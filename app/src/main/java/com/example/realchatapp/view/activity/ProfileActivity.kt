package com.example.realchatapp.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.realchatapp.databinding.ActivityProfileBinding
import com.example.realchatapp.model.profile.ActiveStatus
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.utils.Utils
import com.example.realchatapp.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private var receiverId: String? = null
    private var senderUserId: String? = null
    private var currentState: String? = null
    private var mauth: FirebaseAuth? = null
    var ref: DatabaseReference? = null
    var chatrequestref: DatabaseReference? = null
    var contactsRef: DatabaseReference? = null
    private var notificationRef: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initValue()
        getContacts()

    }

    private fun getContacts() {
        ref!!.child("Users").child(receiverId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("name")
                    ) {
                        val retrieveusername = dataSnapshot.child("name").value.toString()
                        val retrieveuserstatus = dataSnapshot.child("status").value.toString()
                        dataSnapshot.child("image").value.toString()
                        binding.visitUserName.text = retrieveusername
                        binding.visitStatus.text = retrieveuserstatus
                        manageChatRequest()
                    } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                        val retrieveusername = dataSnapshot.child("name").value.toString()
                        val retrieveuserstatus = dataSnapshot.child("status").value.toString()
                        binding.visitUserName.text = retrieveusername
                        binding.visitStatus.text = retrieveuserstatus
                        manageChatRequest()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    private fun initValue() {
        mauth = FirebaseAuth.getInstance()
        senderUserId = mauth!!.currentUser!!.uid
        receiverId = intent.getStringExtra("visit_user_id").toString()

        currentState = "new"
        if (senderUserId == receiverId) binding.sendMessageRequestButton.visibility =
            View.INVISIBLE else binding.sendMessageRequestButton.visibility = View.VISIBLE

        ref = FirebaseDatabase.getInstance().reference
        chatrequestref = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        notificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
    }


    private fun manageChatRequest() {
        chatrequestref!!.child(senderUserId!!).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(receiverId!!)) {
                    val reuest_type =
                        dataSnapshot.child(receiverId!!).child("request_type").value.toString()
                    if (reuest_type == "sent") {
                        currentState = "request_sent"
                        binding.sendMessageRequestButton.text = "  Cancel Chat Request  "
                    } else if (reuest_type == "received") {
                        currentState = "request_received"
                        binding.sendMessageRequestButton.text = "  Accept Chat Request  "
                        binding.declineMessageRequestButton.visibility = View.VISIBLE
                        binding.declineMessageRequestButton.isEnabled = true
                        binding.declineMessageRequestButton.setOnClickListener { CancelChatRequest() }
                    }
                } else {
                    contactsRef!!.child(senderUserId!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.hasChild(receiverId!!)) {
                                    currentState = "friends"
                                    binding.sendMessageRequestButton.text = " Remove this contact "
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        binding.sendMessageRequestButton.setOnClickListener {
            binding.sendMessageRequestButton.isEnabled = false
            if (currentState == "new") {
                sendChatRequest()
            }
            if (currentState == "request_sent") {
                CancelChatRequest()
            }
            if (currentState == "request_received") {
                acceptChatRequest()
            }
            if (currentState == "friends") {
                removeSpecificChatRequest()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun removeSpecificChatRequest() {
        contactsRef!!.child(senderUserId!!).child(receiverId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactsRef!!.child(receiverId!!).child(receiverId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.sendMessageRequestButton.isEnabled = true
                                binding.sendMessageRequestButton.text = "  Send Request  "
                                currentState = "new"
                                binding.declineMessageRequestButton.visibility = View.INVISIBLE
                                binding.declineMessageRequestButton.isEnabled = false
                            }
                        }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun acceptChatRequest() {
        contactsRef!!.child(senderUserId!!).child(receiverId!!)
            .child("Contacts").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactsRef!!.child(receiverId!!).child(senderUserId!!)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                chatrequestref!!.child(senderUserId!!).child(receiverId!!)
                                    .removeValue()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            chatrequestref!!.child(receiverId!!)
                                                .child(senderUserId!!)
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    binding.sendMessageRequestButton.isEnabled =
                                                        true
                                                    currentState = "friends"
                                                    binding.sendMessageRequestButton.text =
                                                        " Remove this contact "
                                                    binding.declineMessageRequestButton.visibility =
                                                        View.INVISIBLE
                                                    binding.declineMessageRequestButton.isEnabled =
                                                        false
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun CancelChatRequest() {
        chatrequestref!!.child(senderUserId!!).child(receiverId!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatrequestref!!.child(receiverId!!).child(senderUserId!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.sendMessageRequestButton.isEnabled = true
                                binding.sendMessageRequestButton.text = "  Send Request  "
                                currentState = "new"
                                binding.declineMessageRequestButton.visibility = View.INVISIBLE
                                binding.declineMessageRequestButton.isEnabled = false
                            }
                        }
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun sendChatRequest() {
        chatrequestref!!.child(senderUserId!!).child(receiverId!!).child("request_type")
            .setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatrequestref!!.child(receiverId!!).child(senderUserId!!)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val chatnotificationMap = HashMap<String, String>()
                                chatnotificationMap["from"] = senderUserId!!
                                chatnotificationMap["type"] = "request"
                                notificationRef!!.child(receiverId!!).push()
                                    .setValue(chatnotificationMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            binding.sendMessageRequestButton.isEnabled = true
                                            currentState = "request_sent"
                                            binding.sendMessageRequestButton.text =
                                                "  Cancel Chat Request  "
                                        }
                                    }
                            }
                        }
                }
            }
    }



    override fun onStart() {
        super.onStart()
        userStatusUpdate("online")

    }

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