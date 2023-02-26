package com.example.realchat.view.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.model.message.Messages
import com.example.realchat.utils.DBReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class MessageAdapter(
    private var UserMessageList: ArrayList<Messages>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var userRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_messages_layout, parent, false)
        val messageViewHolder = ViewHolder(view)
        mAuth = FirebaseAuth.getInstance()
        return messageViewHolder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messagesenderid = mAuth!!.currentUser!!.uid
        val messages = UserMessageList[position]
        val fromuserid = messages.from
        val frommessagetype = messages.type
        userRef = fromuserid.let { FirebaseDatabase.getInstance().reference.child("Users").child(it) }
        userRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    dataSnapshot.child("image").value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        holder.receivermessagetext.visibility = View.GONE
        holder.receiverprofileimage.visibility = View.GONE
        holder.sendermessagetext.visibility = View.GONE
        holder.messageSenderPicture.visibility = View.GONE
        holder.messageReceiverPicture.visibility = View.GONE
        if (frommessagetype == "text") {
            if (fromuserid == messagesenderid) {
                holder.sendermessagetext.visibility = View.VISIBLE
                holder.sendermessagetext.setBackgroundResource(R.drawable.sender_message_layout)
                holder.sendermessagetext.text = (messages.message+ "\n \n" + messages.time) + " - " + messages.date
            } else {
                holder.receivermessagetext.visibility = View.VISIBLE
                holder.receiverprofileimage.visibility = View.VISIBLE
                holder.receivermessagetext.setBackgroundResource(R.drawable.receiver_message_layout)
                holder.receivermessagetext.text = (messages.message + "\n \n" + messages.time) + " - " + messages.date
            }
        } else if (frommessagetype == "image") {
            if (fromuserid == messagesenderid) {
                holder.messageSenderPicture.visibility = View.VISIBLE
                Picasso.get().load(messages.message).into(holder.messageSenderPicture)
            } else {
                holder.messageReceiverPicture.visibility = View.VISIBLE
                holder.messageSenderPicture.visibility = View.INVISIBLE
                Picasso.get().load(messages.message).into(holder.messageReceiverPicture)
            }
        } else if (frommessagetype == "pdf" || frommessagetype == "docx") {
            if (fromuserid == messagesenderid) {
                holder.messageSenderPicture.visibility = View.VISIBLE
                Picasso.get().load(messages.message).into(holder.messageSenderPicture)
            } else {
                holder.messageReceiverPicture.visibility = View.VISIBLE
                holder.messageSenderPicture.visibility = View.INVISIBLE
                Picasso.get().load(messages.message).into(holder.messageReceiverPicture)
            }
        }
        if (fromuserid == messagesenderid) {
            holder.itemView.setOnClickListener {
                if (UserMessageList[position].type == "pdf" || UserMessageList[position].type == "docx"
                ) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me",
                        "Download and view content",
                        "Cancel",
                        "Delete for everyone"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position, holder)
                        } else if (which == 1) {
                            val intent = Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    UserMessageList[position].message
                                )
                            )
                            holder.itemView.context.startActivity(intent)
                        } else if (which == 2) {
                            //for cancel do not do anything
                        } else if (which == 3) {
                            deleteMessageForEveryone(position, holder)
                        }
                    }
                    builder.show()
                } else if (UserMessageList[position].type.equals("text")) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me", "Cancel", "Delete for everyone"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { _, which ->
                        if (which == 0) {
                            deleteSentMessage(position, holder)
                        } else if (which == 1) {
                            //for cancel do not do anything
                        } else if (which == 2) {
                            deleteMessageForEveryone(position, holder)
                        }
                    }
                    builder.show()
                } else if (UserMessageList[position].type.equals("image")) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me", "View This Image", "Cancel", "Delete for everyone"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position, holder)
                        } else if (which == 3) {
                            deleteMessageForEveryone(position, holder)
                        }
                    }
                    builder.show()
                }
            }
        } else {
            holder.itemView.setOnClickListener {
                if (UserMessageList[position].type == "pdf" || UserMessageList[position].type == "docx"
                ) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me", "Download and view content", "Cancel"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { dialog, which ->
                        if (which == 0) {
                            deleteReceiveMessage(position, holder)
                        } else if (which == 1) {
                            val intent = Intent(
                                Intent.ACTION_VIEW, Uri.parse(
                                    UserMessageList[position].message
                                )
                            )
                            holder.itemView.context.startActivity(intent)
                        }
                    }
                    builder.show()
                } else if (UserMessageList[position].type.equals("text")) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me", "Cancel"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { dialog, which ->
                        if (which == 0) {
                            deleteReceiveMessage(position, holder)
                        }
                    }
                    builder.show()
                } else if (UserMessageList[position].type.equals("image")) {
                    val options = arrayOf<CharSequence>(
                        "Delete for me", "View This Image", "Cancel"
                    )
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message?")
                    builder.setItems(
                        options
                    ) { dialog, which ->
                        if (which == 0) {
                            deleteReceiveMessage(position, holder)
                        }
                    }
                    builder.show()
                }
            }
        }
    }

    private fun deleteSentMessage(position: Int, holder: ViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        UserMessageList[position].from.let {
            UserMessageList[position].to.let { it1 ->
                rootRef.child("Messages").child(it)
                    .child(it1)
                    .child(UserMessageList[position].messageID)
                    .removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            notifyItemRemoved(position)
                            UserMessageList.removeAt(position)
                            notifyItemRangeChanged(position, UserMessageList.size)
                            Toast.makeText(
                                holder.itemView.context,
                                "Message deleted...",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(holder.itemView.context, "Error...", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun deleteReceiveMessage(position: Int, holder: ViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages").child(UserMessageList[position].to)
            .child(UserMessageList[position].from)
            .child(UserMessageList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notifyItemRemoved(position)
                    UserMessageList.removeAt(position)
                    notifyItemRangeChanged(position, UserMessageList.size)
                    Toast.makeText(
                        holder.itemView.context,
                        "Message deleted...",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(holder.itemView.context, "Error...", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteMessageForEveryone(position: Int, holder: ViewHolder) {
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages").child(UserMessageList[position].from)
            .child(UserMessageList[position].to)
            .child(UserMessageList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    DBReference.rootRef.child("Messages").child(UserMessageList[position].to)
                        .child(UserMessageList[position].from)
                        .child(UserMessageList[position].messageID)
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                notifyItemRemoved(position)
                                UserMessageList.removeAt(position)
                                notifyItemRangeChanged(position, UserMessageList.size)
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Message deleted...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Error...",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(holder.itemView.context, "Error...", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun getItemCount(): Int {
        return UserMessageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sendermessagetext: TextView
        var receivermessagetext: TextView
        var receiverprofileimage: CircularImageView
        var messageSenderPicture: ImageView
        var messageReceiverPicture: ImageView

        init {
            sendermessagetext = itemView.findViewById(R.id.sender_message_text)
            receivermessagetext = itemView.findViewById(R.id.receiver_message_text)
            receiverprofileimage = itemView.findViewById(R.id.message_profile_image)
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view)
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view)
        }
    }




}