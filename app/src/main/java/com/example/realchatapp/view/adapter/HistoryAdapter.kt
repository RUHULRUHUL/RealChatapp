package com.example.realchatapp.view.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.realchatapp.databinding.UsersDisplayLayoutBinding
import com.example.realchatapp.model.message.Messages
class HistoryAdapter(
    private var userMessageList: ArrayList<Messages>
) : RecyclerView.Adapter<HistoryAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            UsersDisplayLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val messages = userMessageList[position]
        holder.binding.usersProfileName.text = messages.from
        holder.binding.usersStatus.text = messages.message
    }


    override fun getItemCount(): Int {
        return userMessageList.size
    }

    class ChatViewHolder(val binding: UsersDisplayLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}

