package com.example.realchat.helper

import com.example.realchat.model.profile.UserProfile

interface GroupMemberAddCallBack {
    fun onSelectUserList(list: ArrayList<UserProfile>)
}