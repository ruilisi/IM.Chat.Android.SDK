package com.chat.android.im.message

import com.chat.android.im.activity.ChatActivity
import com.chat.android.im.bean.*
import com.chat.android.im.config.RLS
import com.chat.android.im.database.DBInstance
import com.chat.android.im.helper.IChatMessageRefresh
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Manager message send and receive
 */
object MessageManager {

    private var chatMessageRefresh: IChatMessageRefresh? = null
    private val sendLock = ReentrantLock()


    /**
     *  Refresh view
     */
    private fun sendMsgToView(chatMessage: ChatMessage) {
        if (chatMessage.msgBody.message == "Welcome" || chatMessage.msgBody.message == RLS.getInstance().getDataConfig().welcome) {
            chatMessageRefresh?.onRefreshView(chatMessage)
        } else {
            if (!chatMessage.reSend) {
                chatMessage.reSend = true
                if (chatMessage.msgStatus == MsgStatus.SEND) {
                    chatMessageRefresh?.onAddChatSendingMessage(chatMessage)
                }
                if (chatMessage.msgType != MsgType.DEFAULT) {
                    if (ChatActivity.localMessageCount > 20) {
                        ChatActivity.localMessageCount = ChatActivity.localMessageCount.dec()
                        DBInstance.getInstance().getChatMessageDao().delete(DBInstance.getInstance().getChatMessageDao().loadFirstMessage())
                    }
                    DBInstance.getInstance().getChatMessageDao().insert(chatMessage)
                    chatMessageRefresh?.onRefreshView(chatMessage)
                }
            }

        }
    }

    private fun sendListMsgToView(chatMessage: ArrayList<ChatMessage>, isMissedHistory: Boolean) {

        if (isMissedHistory || ChatActivity.localMessageCount < 20) {
            DBInstance.getInstance().getChatMessageDao().insert(chatMessage)
            ChatActivity.localMessageCount = ChatActivity.localMessageCount + chatMessage.size
        }

        chatMessageRefresh?.onRefreshViews(chatMessage, isMissedHistory)
    }

    fun setChatMessageRefreshListener(chatMessageRefresh: IChatMessageRefresh?) {
        this.chatMessageRefresh = chatMessageRefresh
    }

    /**
     * Send sync message
     */
    fun syncWithLockSendMsg(chatMessage: ChatMessage) {
        sendLock.withLock { sendMsg(chatMessage) }
    }

    fun syscWithLockReceiveHistoryMsg(chatMessage: ArrayList<ChatMessage>) {
        sendLock.withLock { receiveHistoryMsg(chatMessage) }
    }

    fun syscWithLocakReceiveMissedHistoryMsg(chatMessage: ArrayList<ChatMessage>) {
        sendLock.withLock { receiveMissedHistoryMsg(chatMessage) }
    }

    private fun receiveHistoryMsg(chatMessage: ArrayList<ChatMessage>) {
        sendListMsgToView(chatMessage, false)
    }

    private fun receiveMissedHistoryMsg(chatMessage: ArrayList<ChatMessage>) {
        sendListMsgToView(chatMessage, true)
    }

    private fun sendMsg(chatMessage: ChatMessage) {
        when (chatMessage.msgStatus) {
            MsgStatus.SEND -> {
                sendMsgToView(chatMessage)
                sendMsgToServer(chatMessage)
            }
            MsgStatus.RECEIVE -> {
                sendMsgToView(chatMessage)
            }
            else -> {
                sendMsgToServer(chatMessage)
            }
        }
    }

    private fun sendMsgToServer(chatMessage: ChatMessage) {
        if (chatMessage.msgType == MsgType.DEFAULT_C) {
            chatMessageRefresh?.onAddChatSendingMessage(chatMessage)
        }
        chatMessageRefresh?.onSendMessage(chatMessage)
    }
}