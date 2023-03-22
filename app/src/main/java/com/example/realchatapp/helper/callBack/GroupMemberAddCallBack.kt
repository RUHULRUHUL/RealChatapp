package com.example.realchatapp.helper.callBack

import com.example.realchatapp.model.profile.User

interface GroupMemberAddCallBack {
    fun onSelectUserList(list: ArrayList<User>)
}