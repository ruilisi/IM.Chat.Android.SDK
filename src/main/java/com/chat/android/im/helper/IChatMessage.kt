package com.chat.android.im.helper

import com.chat.android.im.bean.ChatMessage

/**
 * Created by Ryan on 2020/9/2.
 */
interface IChatMessage {
    fun getItemList(): MutableList<ChatMessage>?
    fun notifyItemChanged(position: Int)
    fun initChatMsgListView(chatMessageList: List<ChatMessage>, dismissRefreshLoading: Boolean = true)
}