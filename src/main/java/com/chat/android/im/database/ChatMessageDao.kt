package com.chat.android.im.database

import androidx.room.*
import com.chat.android.im.bean.ChatMessage

/**
 * Created by Ryan on 2020/9/15.
 */
@Dao
interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg chatMessage: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatMessage: List<ChatMessage>)

    @Update
    fun update(vararg chatMessage: ChatMessage)

    @Query("SELECT * FROM ChatMessage WHERE (tsc_date < :tscTime OR (tsc_date == :tscTime AND ts_date<:tsTime)) AND ts_date != :tsTime ORDER BY tsc_date DESC , ts_date DESC LIMIT :preLoadHistoryCount ")
    fun loadMessageOrderByDesc(preLoadHistoryCount: Int, tscTime: Long, tsTime: Long): List<ChatMessage>

    @Query("SELECT * FROM ChatMessage")
    fun loadAll(): List<ChatMessage>

    @Query("SELECT * FROM ChatMessage ORDER BY ts_date LIMIT 1")
    fun loadFirstMessage(): ChatMessage

    @Query("SELECT * FROM ChatMessage ORDER BY ts_date DESC LIMIT 1")
    fun loadLastMessage(): ChatMessage

    @Query("SELECT COUNT(uuid) FROM ChatMessage")
    fun loadMessageCount(): Long

    @Delete
    fun delete(chatMessage: List<ChatMessage>)

    @Delete
    fun delete(vararg chatMessage: ChatMessage)
}