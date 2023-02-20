package com.example.realchat.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.databinding.FragmentChatsBinding
import com.example.realchat.databinding.UsersDisplayLayoutBinding
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.view.activity.ChatActivity
import com.example.realchat.view.adapter.ActiveChatAdapter
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var userRef: DatabaseReference
    private lateinit var chatRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater)
        initValue()

        chatRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(userID)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        binding.onlineChatRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        val options: FirebaseRecyclerOptions<UserProfile> =
            FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(chatRef, UserProfile::class.java).build()
        val adapter: FirebaseRecyclerAdapter<UserProfile, ChatViewHolder> =
            object : FirebaseRecyclerAdapter<UserProfile, ChatViewHolder>(options) {
                protected override fun onBindViewHolder(
                    holder: ChatViewHolder,
                    position: Int,
                    model: UserProfile
                ) {
                    val userid = getRef(position).key
                    val image = arrayOf("default_image")
                    userRef.child(userid!!)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val name = dataSnapshot.child("name").value.toString()
                                    val status = dataSnapshot.child("status").value.toString()
                                    holder.binding.usersProfileName.text = name
                                    holder.binding.usersStatus.text = """
                            Last seen: 
                            Date  Time
                            """.trimIndent()
                                    if (dataSnapshot.child("userState").hasChild("state")) {
                                        val state = dataSnapshot.child("userState")
                                            .child("state").value.toString()
                                        val date = dataSnapshot.child("userState")
                                            .child("date").value.toString()
                                        val time = dataSnapshot.child("userState")
                                            .child("time").value.toString()
                                        if (state == "online") {
                                            holder.binding.usersStatus.text = "online"
                                        } else if (state == "offline") {
                                            holder.binding.usersStatus.text =
                                                "Last seen: \n$date $time"
                                        }
                                    } else {
                                        holder.binding.usersStatus.text = "offline"
                                    }
                                    holder.itemView.setOnClickListener {
                                        val chatIntent = Intent(context, ChatActivity::class.java)
                                        chatIntent.putExtra("visit_user_id", userid)
                                        chatIntent.putExtra("visit_user_name", name)
                                        chatIntent.putExtra("visit_image", image[0])
                                        startActivity(chatIntent)
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                    return ChatViewHolder(
                        UsersDisplayLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
            }
        binding.onlineChatRV.adapter = adapter
        adapter.startListening()
    }

    class ChatViewHolder(val binding: UsersDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }


    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()

        userRef = FirebaseDatabase.getInstance().reference

    }


}