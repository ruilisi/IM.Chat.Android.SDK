package com.chat.android.im.message

import com.chat.android.im.bean.*
import com.chat.android.im.config.RLS.Companion.empty
import com.chat.android.im.utils.RandomGenerator
import java.util.*

/**
 * Generate message
 */

/**
 * Generate send message
 */
fun generateBaseSendMessage(msgType: MsgType): ChatMessage {
    val time = System.currentTimeMillis()
    val mMessgae = ChatMessage()
    mMessgae.uuid = UUID.randomUUID().toString()
    mMessgae.msgStatus = MsgStatus.SEND
    mMessgae.sentStatus = MsgSendStatus.SENDING
    mMessgae.msgType = msgType
    mMessgae.ts.date = time
    mMessgae.tsc.date = time
    mMessgae._updatedAt.date = time
    return mMessgae
}

/**
 * Generate receive message
 */
fun generateBaseReceiveMessage(msgType: MsgType): ChatMessage {
    val time = System.currentTimeMillis()
    val mMessgae = ChatMessage()
    mMessgae.uuid = UUID.randomUUID().toString()
    mMessgae.msgStatus = MsgStatus.RECEIVE
    mMessgae.msgType = msgType
    mMessgae.ts.date = time
    mMessgae.tsc.date = time
    mMessgae._updatedAt.date = time
    return mMessgae
}

/**
 * Generate normal message
 */
fun generateBaseNormalMessage(jsonMsg: String, msgType: MsgType = MsgType.DEFAULT): ChatMessage {
    val mMessage = ChatMessage()
    mMessage.uuid = UUID.randomUUID().toString()
    mMessage.msgBody.extra = jsonMsg
    mMessage.msgType = msgType
    return mMessage
}

/**
 * Generate connect message
 */
fun generateConnectMsg(): ConnectIm {
    return ConnectIm()
}

/**
 * Generate login message by token
 * @param count auto number id
 * @param token auth-token
 */
fun generateLoginMsgByTokenMsg(count: Long, token: String?): LoginIm {
    return LoginIm(resume = token, id = "$count")
}

/**
 * Generate sub message
 * @param uniqueId the unique id
 */
fun generateSubMsg(uniqueId: String?, type: MsgGenerageType): SubIm {
    return SubIm(id = RandomGenerator().hexString(), unique = uniqueId, type = type)
}

/**
 * Generate send message
 */
fun generateSendMsg(count: Long, msg: String, rid: String?): SendMsg {
    return SendMsg(id = "$count", _id = RandomGenerator().hexString(), message = msg, rid = rid)
}

/**
 * Generate history message
 */
fun generateHistoryMsg(count: Long, preLoadHistoryCount: Int, rid: String?, time: String): HistoryIm {
    val t = if (time == empty) System.currentTimeMillis().toString() else time
    return HistoryIm(id = "${count}", rid = rid, count = preLoadHistoryCount, time = t)
}

/**
 *  Generate load missed history message
 */
fun generateLoadMissedHistoryMsg(count: Long, rid: String?, date: Long): LoadMissedMessagesSend {
    val d = if (date == 0L) System.currentTimeMillis() else date
    return LoadMissedMessagesSend(id = "$count", rid = rid, `$date` = d)
}

/**
 *  Generate ping
 */
fun generatePing(): Ping {
    return Ping()
}

/**
 *  Generate ping
 */
fun generatePong(): Pong {
    return Pong()
}
