package com.example.realchat.view.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.model.profile.User
import com.example.realchat.view.activity.ProfileActivity

class UserSearchAdapter(
    private val list: ArrayList<User>,
    val context: Context
) : RecyclerView.Adapter<UserSearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.users_display_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = list[position]

        Log.d("userName", "name " + profile.name.toString())
        Log.d("userName", "status " + profile.status.toString())
        Log.d("userName", "phone " + profile.phone.toString())

        holder.username.text = profile.name
        holder.itemView.setOnClickListener {
            val chatIntent = Intent(context, ProfileActivity::class.java)
            chatIntent.putExtra("visit_user_id", profile.uid)
            chatIntent.putExtra("visit_user_name", profile.name)
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