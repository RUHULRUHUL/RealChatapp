package com.example.realchatapp.helper.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.realchatapp.model.message.Messages
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Messages)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleMessage(users: List<Messages>)

    @Query("SELECT * FROM Messages")
    fun getAllMessages(): Flow<List<Messages>>

    @Query("SELECT * FROM Messages")
    fun getMessageList(): List<Messages>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllMessage(allMessageList: List<Messages>)

    @Query("SELECT * FROM Messages")
    fun getAllChatMsgList(): List<Messages>

    @Query("DELETE  FROM Messages")
    fun  deleteAllMessages()
}