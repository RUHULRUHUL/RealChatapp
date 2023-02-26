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
import com.example.realchat.model.profile.User
import com.example.realchat.utils.DBReference.Companion.userRef
import com.example.realchat.view.activity.ChatActivity
import com.example.realchat.view.activity.ProfileActivity
import com.example.realchat.view.fragment.ChatsFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AllUsersAdapter(
    options: FirebaseRecyclerOptions<User>,
    val context: Context
) : FirebaseRecyclerAdapter<User, AllUsersAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            UsersDisplayLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: User) {
        holder.binding.usersProfileName.text = model.name
        val userid = getRef(position).key
        if (userid != null) {
            userRef.child(userid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.child("UserState").hasChild("state")) {
                                val state = dataSnapshot.child("UserState")
                                    .child("state").value.toString()

                                val time = dataSnapshot.child("UserState")
                                    .child("time").value.toString()

                                val date = dataSnapshot.child("UserState")
                                    .child("date").value.toString()

                                holder.binding.usersStatus.text = "$time-$date"

                                if (state.equals("online", false)) {
                                    holder.binding.stateImg.visibility = View.VISIBLE
                                } else {
                                    holder.binding.stateImg.visibility = View.GONE
                                }
                            } else {
                                holder.binding.stateImg.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        holder.itemView.setOnClickListener {
            val chatIntent = Intent(context, ProfileActivity::class.java)
            chatIntent.putExtra("visit_user_id", getRef(position).key)
            chatIntent.putExtra("visit_user_name", model.name)
            context.startActivity(chatIntent)
        }
    }

    class ViewHolder(val binding: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}