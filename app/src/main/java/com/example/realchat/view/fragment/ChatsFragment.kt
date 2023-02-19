package com.example.realchat.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.databinding.FragmentChatsBinding
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.utils.Validator
import com.example.realchat.view.adapter.ActiveChatAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var adapter: ActiveChatAdapter
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater)
        initValue()

        Validator.showToast(requireContext(), "ChatsFragment")

        val options: FirebaseRecyclerOptions<UserProfile> =
            FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(userRef, UserProfile::class.java)
                .build()
        adapter = ActiveChatAdapter(options,requireContext())
        binding.onlineChatRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.onlineChatRV.adapter = adapter

        return binding.root
    }

/*    private fun showOnlineUser() {
            val options: FirebaseRecyclerOptions<UserProfile> =
                FirebaseRecyclerOptions.Builder<UserProfile>()
                    .setQuery(userRef, UserProfile::class.java).build()
            val adapter: FirebaseRecyclerAdapter<UserProfile, ChatViewHolder> =
                object : FirebaseRecyclerAdapter<UserProfile, ChatViewHolder>(options) {
                    protected override fun onBindViewHolder(
                        holder: ChatViewHolder,
                        position: Int,
                        model: UserProfile
                    ) {
                        val userid = getRef(position).key
                        val image = arrayOf("default_image")
                        userRef.child(mAuth.uid.toString())
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        val name = dataSnapshot.child("name").value.toString()
                                        val status = dataSnapshot.child("status").value.toString()
                                        val phone = dataSnapshot.child("phone").value.toString()
                                        holder.username.text = name
                                        holder.userStatus.text = """
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
                                                holder.userStatus.text = "online"
                                            } else if (state == "offline") {
                                                holder.userStatus.text = "Last seen: \n$date $time"
                                            }
                                        } else {
                                            holder.userStatus.text = "offline"
                                        }
                                        holder.itemView.setOnClickListener {
                                            val chatIntent = Intent(context, UserProfile::class.java)
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
                        val view: View =
                            LayoutInflater.from(context)
                                .inflate(R.layout.users_display_layout, parent, false)
                        return ChatViewHolder(view)
                    }
                }
            binding.onlineChatRV.adapter = adapter
            adapter.startListening()
    }*/

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()

        userRef = FirebaseDatabase.getInstance().reference

    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


}