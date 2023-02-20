package com.example.realchat.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.realchat.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private var reciever_id: String? = null
    private var sender_user_id: String? = null
    private var current_state: String? = null
    private var mauth: FirebaseAuth? = null
    var ref: DatabaseReference? = null
    var chatrequestref: DatabaseReference? = null
    var contactsRef: DatabaseReference? = null
    var NotificationRef: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initValue()
        getContacts()

    }

    private fun getContacts() {
        ref!!.child("Users").child(reciever_id!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild("name")
                    ) {
                        val retrieveusername = dataSnapshot.child("name").value.toString()
                        val retrieveuserstatus = dataSnapshot.child("status").value.toString()
                        val retrieveuserimage = dataSnapshot.child("image").value.toString()
                        binding.visitUserName.text = retrieveusername
                        binding.visitStatus.text = retrieveuserstatus
                        ManageChatRequest()
                    } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                        val retrieveusername = dataSnapshot.child("name").value.toString()
                        val retrieveuserstatus = dataSnapshot.child("status").value.toString()
                        binding.visitUserName.text = retrieveusername
                        binding.visitStatus.text = retrieveuserstatus
                        ManageChatRequest()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }


    private fun initValue() {
        mauth = FirebaseAuth.getInstance()
        sender_user_id = mauth!!.currentUser!!.uid
        reciever_id = intent.extras!!["visit_user_id"].toString()


        current_state = "new"
        if (sender_user_id == reciever_id) binding.sendMessageRequestButton.visibility =
            View.INVISIBLE else binding.sendMessageRequestButton.visibility = View.VISIBLE

        ref = FirebaseDatabase.getInstance().reference
        chatrequestref = FirebaseDatabase.getInstance().reference.child("Chat Requests")
        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        NotificationRef = FirebaseDatabase.getInstance().reference.child("Notifications")
    }


    private fun ManageChatRequest() {
        chatrequestref!!.child(sender_user_id!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(reciever_id!!)) {
                    val reuest_type =
                        dataSnapshot.child(reciever_id!!).child("request_type").value.toString()
                    if (reuest_type == "sent") {
                        current_state = "request_sent"
                        binding.sendMessageRequestButton.text = "  Cancel Chat Request  "
                    } else if (reuest_type == "received") {
                        current_state = "request_received"
                        binding.sendMessageRequestButton.text = "  Accept Chat Request  "
                        binding.declineMessageRequestButton.visibility = View.VISIBLE
                        binding.declineMessageRequestButton.isEnabled = true
                        binding.declineMessageRequestButton.setOnClickListener { CancelChatRequest() }
                    }
                } else {
                    contactsRef!!.child(sender_user_id!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.hasChild(reciever_id!!)) {
                                    current_state = "friends"
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
            if (current_state == "new") {
                SendChatRequest()
            }
            if (current_state == "request_sent") {
                CancelChatRequest()
            }
            if (current_state == "request_received") {
                AcceptChatRequest()
            }
            if (current_state == "friends") {
                RemoveSpecificChatRequest()
            }
        }
    }

    private fun RemoveSpecificChatRequest() {
        contactsRef!!.child(sender_user_id!!).child(reciever_id!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactsRef!!.child(reciever_id!!).child(sender_user_id!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.sendMessageRequestButton.isEnabled = true
                                binding.sendMessageRequestButton.text = "  Send Request  "
                                current_state = "new"
                                binding.declineMessageRequestButton.visibility = View.INVISIBLE
                                binding.declineMessageRequestButton.isEnabled = false
                            }
                        }
                }
            }
    }

    private fun AcceptChatRequest() {
        contactsRef!!.child(sender_user_id!!).child(reciever_id!!)
            .child("Contacts").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactsRef!!.child(reciever_id!!).child(sender_user_id!!)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                chatrequestref!!.child(sender_user_id!!).child(reciever_id!!)
                                    .removeValue()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            chatrequestref!!.child(reciever_id!!)
                                                .child(sender_user_id!!)
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    binding.sendMessageRequestButton.isEnabled =
                                                        true
                                                    current_state = "friends"
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

    private fun CancelChatRequest() {
        chatrequestref!!.child(sender_user_id!!).child(reciever_id!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatrequestref!!.child(reciever_id!!).child(sender_user_id!!)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.sendMessageRequestButton.isEnabled = true
                                binding.sendMessageRequestButton.text = "  Send Request  "
                                current_state = "new"
                                binding.declineMessageRequestButton.visibility = View.INVISIBLE
                                binding.declineMessageRequestButton.isEnabled = false
                            }
                        }
                }
            }
    }

    private fun SendChatRequest() {
        chatrequestref!!.child(sender_user_id!!).child(reciever_id!!).child("request_type")
            .setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatrequestref!!.child(reciever_id!!).child(sender_user_id!!)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val chatnotificationMap = HashMap<String, String>()
                                chatnotificationMap["from"] = sender_user_id!!
                                chatnotificationMap["type"] = "request"
                                NotificationRef!!.child(reciever_id!!).push()
                                    .setValue(chatnotificationMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            binding.sendMessageRequestButton.isEnabled = true
                                            current_state = "request_sent"
                                            binding.sendMessageRequestButton.text =
                                                "  Cancel Chat Request  "
                                        }
                                    }
                            }
                        }
                }
            }
    }


}