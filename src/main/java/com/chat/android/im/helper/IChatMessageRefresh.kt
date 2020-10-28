package com.chat.android.im.helper

import com.chat.android.im.bean.ChatMessage

/**
 * Created by Ryan on 2020/9/8.
 */
interface IChatMessageRefresh {

    fun onSendMessage(chatMessage: ChatMessage)
    fun onAddChatSendingMessage(chatMessage: ChatMessage)
    fun onRefreshView(chatMessage: ChatMessage)
    fun onRefreshViews(chatMessage: ArrayList<ChatMessage>,isMissedMessage:Boolean)
}