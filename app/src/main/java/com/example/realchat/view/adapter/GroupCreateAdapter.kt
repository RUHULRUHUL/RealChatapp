package com.example.realchat.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.realchat.R
import com.example.realchat.helper.callBack.GroupMemberAddCallBack
import com.example.realchat.model.profile.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class GroupCreateAdapter(
    private val options: FirebaseRecyclerOptions<User>,
    val context: Context
) : FirebaseRecyclerAdapter<User, GroupCreateAdapter.ViewHolder>(options) {

    private lateinit var callBack: GroupMemberAddCallBack
    private var list = ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.add_user_group_row_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: User) {
        holder.username.text = model.name
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                list.add(model)
                callBack.onSelectUserList(list)
            } else {
                list.removeAt(position)
                callBack.onSelectUserList(list)
            }

        }

    }

    fun setCallBackListener(listener: GroupMemberAddCallBack) {
        callBack = listener
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var checkBox: CheckBox

        init {
            username = itemView.findViewById(R.id.userNameTxt)
            checkBox = itemView.findViewById(R.id.radioSelectBtn)

        }
    }

}