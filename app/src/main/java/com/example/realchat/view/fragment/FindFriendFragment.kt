package com.example.realchat.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchat.R
import com.example.realchat.databinding.FragmentChatsBinding
import com.example.realchat.databinding.FragmentFindFriendBinding
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.view.adapter.AllUsersAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FindFriendFragment : Fragment() {
    private lateinit var binding: FragmentFindFriendBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: String
    private lateinit var adapter: AllUsersAdapter
    private lateinit var userRef: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFindFriendBinding.inflate(inflater)
        initValue()

        binding.toolbar.mainAppBar.title = "All Users"

        displayUsers()
        return binding.root
    }

    private fun displayUsers() {
        val options: FirebaseRecyclerOptions<UserProfile> =
            FirebaseRecyclerOptions.Builder<UserProfile>()
                .setQuery(userRef, UserProfile::class.java)
                .build()

        adapter = AllUsersAdapter(options, requireContext())
        binding.findFriendRV.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.findFriendRV.adapter = adapter

    }

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        userID = mAuth.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

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