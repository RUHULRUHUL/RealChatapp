package com.example.realchat.view.fragment

import android.annotation.SuppressLint
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
import com.example.realchat.model.profile.User
import com.example.realchat.view.activity.ChatActivity
import com.example.realchat.view.activity.SearchUserActivity
import com.example.realchat.view.adapter.ContactAdapter
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
        clickEvent()
        return binding.root
    }

    private fun getContacts() {
        val options: FirebaseRecyclerOptions<User> =
            FirebaseRecyclerOptions.Builder<User>()
                .setQuery(chatRef, User::class.java).build()

        binding.onlineChatRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
       val  adapter = ContactAdapter(options = options, requireContext())
        binding.onlineChatRV.adapter = adapter
    }

    private fun clickEvent() {
        binding.searchUsers.setOnClickListener {
            startActivity(Intent(requireContext(), SearchUserActivity::class.java))
        }
    }


    override fun onStart() {
        super.onStart()
        val options: FirebaseRecyclerOptions<User> =
            FirebaseRecyclerOptions.Builder<User>()
                .setQuery(chatRef, User::class.java).build()
        val adapter: FirebaseRecyclerAdapter<User, ChatViewHolder> =
            object : FirebaseRecyclerAdapter<User, ChatViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: ChatViewHolder,
                    position: Int,
                    model: User) {
                    val userid = getRef(position).key
                    val image = arrayOf("default_image")
                    if (userid != null) {
                        userRef.child(userid)
                            .addValueEventListener(object : ValueEventListener {
                                @SuppressLint("SetTextI18n")
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        val name = dataSnapshot.child("name").value.toString()
                                        holder.binding.usersProfileName.text = name

                                        if (dataSnapshot.child("UserState").hasChild("state")) {
                                            val state = dataSnapshot.child("UserState")
                                                .child("state").value.toString()
                                            val time = dataSnapshot.child("UserState")
                                                .child("time").value.toString()
                                            val date = dataSnapshot.child("UserState")
                                                .child("date").value.toString()
                                            holder.binding.usersStatus.text = "$time-$date"

                                            if (state.equals("online", false)) {
                                                holder.binding.stateImg.visibility = View.VISIBLE
                                            } else {
                                                holder.binding.stateImg.visibility = View.GONE
                                            }
                                        }

                                        holder.itemView.setOnClickListener {
                                            val chatIntent =
                                                Intent(context, ChatActivity::class.java)
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
        RecyclerView.ViewHolder(binding.root)

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()
        binding.toolbar.title = "My Contacts"
        chatRef = FirebaseDatabase.getInstance().reference.child("Contacts").child(userID)
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        binding.onlineChatRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

}