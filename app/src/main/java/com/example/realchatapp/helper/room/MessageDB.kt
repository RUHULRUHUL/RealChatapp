package com.example.realchatapp.helper.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.realchatapp.model.message.Messages

@Database(
    entities = [Messages::class],
    version = 2,
    exportSchema = false
)
abstract class MessageDB : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        var instance: MessageDB? = null

        @Synchronized
        fun getInstance(context: Context): MessageDB? {
            if (instance == null) {
                synchronized(MessageDB::class.java) {
                    if (instance == null) {
                        instance = databaseBuilder(
                            context.applicationContext,
                            MessageDB::class.java,
                            "MessageDB"
                        )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return instance
        }
    }


}