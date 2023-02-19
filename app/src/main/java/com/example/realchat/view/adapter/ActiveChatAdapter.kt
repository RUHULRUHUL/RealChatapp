package com.example.realchat.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.model.profile.UserProfile
import com.example.realchat.view.activity.ChatActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ActiveChatAdapter(
    options: FirebaseRecyclerOptions<UserProfile>,
    val context: Context
) : FirebaseRecyclerAdapter<UserProfile, ActiveChatAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.users_display_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: UserProfile) {
        holder.username.text = model.name
        holder.itemView.setOnClickListener {
            val chatIntent = Intent(context, ChatActivity::class.java)
            chatIntent.putExtra("visit_user_id", getRef(position).key)
            chatIntent.putExtra("visit_user_name", model.name)
            context.startActivity(chatIntent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var userStatus: TextView

        init {
            username = itemView.findViewById(R.id.users_profile_name)
            userStatus = itemView.findViewById(R.id.users_status)
        }
    }

}