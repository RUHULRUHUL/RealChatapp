package com.example.realchat.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.databinding.CustomMessagesLayoutBinding
import com.example.realchat.model.message.Messages
import com.firebase.ui.database.paging.DatabasePagingOptions
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessagePagingAdapter
    (options: DatabasePagingOptions<Messages>) :
    FirebaseRecyclerPagingAdapter<Messages, MessagePagingAdapter.ViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomMessagesLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, model: Messages) {

    }

    class ViewHolder(val binding :CustomMessagesLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}