package com.example.realchatapp.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.realchatapp.R
import com.example.realchatapp.databinding.CustomMessagesLayoutBinding
import com.example.realchatapp.model.message.Messages
import com.example.realchatapp.utils.DBReference

class AdChatAdapter(private val context: Context) : ListAdapter<Messages, ViewHolder>(DiffCallbackMessages()) {

     lateinit var messageList: MutableList<Messages>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CustomMessagesLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = messageList[position]
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
}

class ViewHolder(val binding: CustomMessagesLayoutBinding) :
    RecyclerView.ViewHolder(binding.root)


class DiffCallbackMessages : DiffUtil.ItemCallback<Messages>() {
    override fun areItemsTheSame(oldItem: Messages, newItem: Messages): Boolean {
        return oldItem.messageID == newItem.messageID
    }

    override fun areContentsTheSame(oldItem: Messages, newItem: Messages): Boolean {
        return oldItem == newItem
    }
}
