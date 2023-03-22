package com.example.realchatapp.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realchatapp.R
import com.example.realchatapp.databinding.FragmentChatHistoryBinding
import com.example.realchatapp.databinding.FragmentChatsBinding
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.utils.DBReference
import com.example.realchatapp.view.adapter.HistoryAdapter
import com.example.realchatapp.view.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ChatHistoryFragment : Fragment() {
    private lateinit var binding: FragmentChatHistoryBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var lastMsgList: ArrayList<Messages>
    private lateinit var adapter: HistoryAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatHistoryBinding.inflate(inflater)
        initValue()
        getLastMessage()
        return binding.root
    }

    private fun getLastMessage() {
        Firebase.firestore.collection("history")
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val messages = document.toObject(Messages::class.java)
                    lastMsgList.add(messages)
                }
                binding.historyRV.layoutManager = LinearLayoutManager(requireContext())
                adapter = HistoryAdapter(lastMsgList)
                binding.historyRV.setHasFixedSize(true)
                binding.historyRV.adapter = adapter
            }
    }

    private fun initValue() {
        mAuth = FirebaseAuth.getInstance()
        lastMsgList = ArrayList<Messages>()
        binding.toolbar.title = "Messages"
    }
}