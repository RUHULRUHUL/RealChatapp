package com.example.realchat.helper

import com.example.realchat.model.profile.User

interface GroupMemberAddCallBack {
    fun onSelectUserList(list: ArrayList<User>)
}