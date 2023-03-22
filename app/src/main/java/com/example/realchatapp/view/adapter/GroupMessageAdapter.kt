package com.example.realchatapp.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.realchatapp.R
import com.example.realchatapp.helper.callBack.MessageDeleteCallBack
import com.example.realchatapp.model.message.GroupMessage
import com.example.realchatapp.utils.DBReference
import com.google.firebase.auth.FirebaseAuth
import com.mikhaellopez.circularimageview.CircularImageView

class GroupMessageAdapter(
    private var messageList: ArrayList<GroupMessage>
) : RecyclerView.Adapter<GroupMessageAdapter.ViewHolder>() {
    private var mAuth: FirebaseAuth? = null
    private lateinit var deleteCallBack:MessageDeleteCallBack
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
        val messages = messageList[position]
        val frommessagetype = messages.type

        holder.receivermessagetext.visibility = View.GONE
        holder.receiverprofileimage.visibility = View.GONE
        holder.sendermessagetext.visibility = View.GONE
        holder.messageSenderPicture.visibility = View.GONE
        holder.messageReceiverPicture.visibility = View.GONE

        if (frommessagetype == "text") {
            if (messages.uid == messagesenderid) {
                holder.sendermessagetext.visibility = View.VISIBLE
                holder.sendermessagetext.setBackgroundResource(R.drawable.sender_message_layout)
                holder.sendermessagetext.text =
                    (messages.message + "\n \n" + messages.time) + " - " + messages.date
            } else {
                holder.receivermessagetext.visibility = View.VISIBLE
                holder.receiverprofileimage.visibility = View.VISIBLE
                holder.receivermessagetext.setBackgroundResource(R.drawable.receiver_message_layout)
                holder.receivermessagetext.text =
                    (messages.message + "\n \n" + messages.time) + " - " + messages.date
            }
        } else if (frommessagetype == "image") {
            if (messages.uid == messagesenderid) {
                holder.messageSenderPicture.visibility = View.VISIBLE
            } else {
                holder.messageReceiverPicture.visibility = View.VISIBLE
                holder.receiverprofileimage.visibility = View.VISIBLE

            }
        } else if (frommessagetype == "pdf" || frommessagetype == "docx") {
            if (messages.uid == messagesenderid) {
                holder.messageSenderPicture.visibility = View.VISIBLE
            } else {
                holder.messageReceiverPicture.visibility = View.VISIBLE
                holder.receiverprofileimage.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            deleteMessage(position, holder)
        }
    }

    fun addChatList(list: List<GroupMessage>) {
        for (i in 0..list.size) {
            messageList.add(i, list[i])
        }
        notifyDataSetChanged()
    }

    fun deleteMessage(callBack: MessageDeleteCallBack){
        this.deleteCallBack = callBack
    }

    private fun deleteMessage(position: Int, holder: ViewHolder) {
        DBReference.uid?.let {
            DBReference.groupRef
                .child("whatsup")
                .child(messageList[position].messageId)
                .removeValue()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        deleteCallBack.onMessageDelete(true)
                        messageList.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Error...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
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

