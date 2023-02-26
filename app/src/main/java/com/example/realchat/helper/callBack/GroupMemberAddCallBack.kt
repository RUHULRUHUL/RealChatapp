package com.example.realchat.helper.callBack

import com.example.realchat.model.profile.User

interface GroupMemberAddCallBack {
    fun onSelectUserList(list: ArrayList<User>)
}