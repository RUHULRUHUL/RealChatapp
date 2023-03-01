package com.example.realchat.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.databinding.CustomMessagesLayoutBinding
import com.example.realchat.model.message.Messages
import com.example.realchat.utils.DBReference
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class OneToOneMessageAdapter(
    private val options: FirebaseRecyclerOptions<Messages>,
) : FirebaseRecyclerAdapter<Messages, OneToOneMessageAdapter.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomMessagesLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Messages) {
        val currentUid = model.from
        val messageType = model.type
        holder.binding.receiverMessageText.visibility = View.GONE
        holder.binding.messageProfileImage.visibility = View.GONE
        holder.binding.senderMessageText.visibility = View.GONE
        holder.binding.messageSenderImageView.visibility = View.GONE
        holder.binding.messageReceiverImageView.visibility = View.GONE
        if (messageType == "text") {
            if (DBReference.uid == currentUid) {
                holder.binding.senderMessageText.visibility = View.VISIBLE
                holder.binding.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout)
                holder.binding.senderMessageText.text =
                    (model.message + "\n \n" + model.time) + " - " + model.date
            } else {
                holder.binding.receiverMessageText.visibility = View.VISIBLE
                holder.binding.messageProfileImage.visibility = View.VISIBLE
                holder.binding.messageReceiverImageView.visibility = View.VISIBLE
                holder.binding.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout)
                holder.binding.receiverMessageText.text =
                    (model.message + "\n \n" + model.time) + " - " + model.date
            }
        }
    }

    class ViewHolder(val binding: CustomMessagesLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

}