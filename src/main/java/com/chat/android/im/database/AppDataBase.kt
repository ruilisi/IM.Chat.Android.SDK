package com.chat.android.im.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chat.android.im.bean.ChatMessage

/**
 * Created by Ryan on 2020/9/15.
 */
@Database(entities = arrayOf(ChatMessage::class), version = 3)
abstract class AppDataBase : RoomDatabase() {
    abstract fun getChatMessageDao(): ChatMessageDao
}