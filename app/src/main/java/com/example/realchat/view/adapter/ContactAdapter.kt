package com.example.realchat.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.databinding.UsersDisplayLayoutBinding
import com.example.realchat.model.profile.Profile
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference.Companion.userRef
import com.example.realchat.utils.Validator
import com.example.realchat.view.activity.ChatActivity
import com.example.realchat.view.activity.ProfileActivity
import com.example.realchat.view.fragment.ChatsFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ContactAdapter (
    options: FirebaseRecyclerOptions<Profile>,
    val context: Context
) : FirebaseRecyclerAdapter<Profile, ContactAdapter.ChatViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            UsersDisplayLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Profile) {
        val userid = getRef(position).key
        val image = arrayOf("default_image")

        holder.binding.usersProfileName.text = model.name
/*
        if (model.activeStatus.state == "online") {
            holder.binding.stateImg.visibility = View.VISIBLE
        } else {
            holder.binding.stateImg.visibility = View.GONE
        }
*/

        holder.itemView.setOnClickListener {
            val chatIntent = Intent(context, ChatActivity::class.java)
            chatIntent.putExtra("visit_user_id", userid)
            chatIntent.putExtra("visit_user_name", model.name)
            chatIntent.putExtra("visit_image", image[0])
            context.startActivity(chatIntent)
        }
    }

    class ChatViewHolder(val binding: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}