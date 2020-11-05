package com.chat.android.im.bean

import androidx.room.*
import com.chat.android.im.config.RLS.Companion.empty
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Created by Ryan on 2020/8/25.
 */

/**
 * 消息类型
 */
enum class MsgType {
    DEFAULT,//默认
    DEFAULT_C,//默认消息（连接后发的默认消息）
    TEXT,  //文本消息
    AUDIO,  //语音消息
    VIDEO,  //视频消息
    IMAGE,  //图片消息
    FILE,  //文件消息
    LOCATION; //位置消息

    class MsgTypeConverter {
        @TypeConverter
        fun toInt(msgType: MsgType): Int = msgType.ordinal

        @TypeConverter
        fun fromInt(int: Int): MsgType = values()[int]
    }
}

/**
 * 消息接收或者发送类型
 */
enum class MsgStatus {
    DEFAULT,//默认
    RECEIVE,  //接收消息
    SEND;//发送消息

    class MsgStatusConverter {
        @TypeConverter
        fun toInt(msgType: MsgStatus): Int = msgType.ordinal

        @TypeConverter
        fun fromInt(int: Int): MsgStatus = values()[int]
    }
}

enum class MsgSendStatus {
    DEFAULT,
    SENDING,  //发送中
    FAILED,  //发送失败
    SENT;//已发送

    class MsgSendStatusConverter {
        @TypeConverter
        fun toInt(msgType: MsgSendStatus): Int = msgType.ordinal

        @TypeConverter
        fun fromInt(int: Int): MsgSendStatus = values()[int]
    }
}

/*********************************************************/

/**
 * Generated message type
 */
enum class MsgGenerageType {
    CONNECT,//连接
    LOGIN,//登录
    SUB_NOTIFICATION,//订阅
    SUB_ROOM_CHANGED,//订阅
    SUB_ROOM_MESSAGE,//订阅
    SEND,//发送
    HISTORY,//历史
    MISSED_HISTORY,//丢失的数据
    PING,//ping
    PONG//pong
}

/**
 * The parent json bean to connect to sever
 */
open class Message

/**
 * The json bean to connect to sever
 */
class ConnectIm(val msg: String = "connect",
                val version: String = "1",
                val support: Array<String> = arrayOf("1", "pre2", "pre1")) : Message()


/**
 * The json bean to login
 */
class LoginIm(val msg: String = "method",
              val method: String = "login",
              @Transient
              val resume: String?,
              val id: String
) : Message() {
    val params: Array<ResumeIm> = arrayOf(ResumeIm(resume))
}

class ResumeIm(val resume: String?)

/**
 * The json bean to login
 */
class SubIm(val msg: String = "sub",
            val id: String,
            var name: String = "stream-notify-user",
            @Transient
            val unique: String?,
            @Transient
            val type: MsgGenerageType
) : Message() {
    @Transient
    var stream = when (type) {
        MsgGenerageType.SUB_ROOM_CHANGED -> "/rooms-changed"
        MsgGenerageType.SUB_NOTIFICATION -> "/notification"
        MsgGenerageType.SUB_ROOM_MESSAGE -> {
            name = "stream-room-messages"
            ""
        }
        else -> ""
    }

    val params: Array<Any> = arrayOf("${unique}${stream}", NotificationIm())
}

class NotificationIm(val useCollection: Boolean = false,
                     val args: Array<String> = emptyArray())

/**
 * The json bean to send msg
 */
class SendMsg(val msg: String = "method",
              val method: String = "sendMessage",
              val id: String,
              @Transient
              val _id: String,
              @Transient
              val rid: String?,
              @Transient
              val message: String) : Message() {
    val params: Array<SendMsgBody> = arrayOf(SendMsgBody(_id, rid, message))
}

class SendMsgBody(val _id: String, val rid: String?, val msg: String)


/**
 * The json bean to load history message
 */
class HistoryIm(val msg: String = "method",
                val method: String = "loadHistory",
                val id: String,
                @Transient
                val rid: String?,
                @Transient
                val count: Int,
                @Transient
                val time: String = System.currentTimeMillis().toString()
) : Message() {
    val params: Array<Any?> = arrayOf(rid, MessageDate(time.toLong()), count, null)
}

/**
 * The json bean to load missed history message
 */
class LoadMissedMessagesSend(val msg: String = "method",
                             val method: String = "loadMissedMessages",
                             val id: String,
                             @Transient
                             val rid: String?,
                             @Transient
                             val `$date`: Long = 0) : Message() {
    val params: Array<Any?> = arrayOf(rid, MessageDate(`$date`))
}

class MessageDate(var `$date`: Long = 0L)
class ChatMessageDate(var date: Long = 0L)


/**
 *  message
 */
class MsgBody {
    var message: String = empty
    var extra: String = empty

    @TypeConverters(Attachments.AttachmentsConverter::class)
    var attachments: Array<Attachments>? = arrayOf()
}

/**
 * The local show message
 */
@Entity
class ChatMessage {
    @PrimaryKey
    var uuid: String = empty
    var msgId: Long = 0

    @TypeConverters(MsgStatus.MsgStatusConverter::class)
    var msgStatus = MsgStatus.DEFAULT

    @Ignore
    var readStatus = 0 //保留字段

    @Ignore
    var reSend = false

    @Embedded(prefix = "ts_")
    var ts = ChatMessageDate()

    @Embedded(prefix = "updateAt_")
    var _updatedAt = ChatMessageDate()

    /**
     * the time to save
     */
    @Embedded(prefix = "tsc_")
    var tsc = ChatMessageDate()

    /**
     * the tiem to show
     */
    @Embedded(prefix = "tsh_")
    var timeShow = ChatMessageDate()

    @TypeConverters(MsgType.MsgTypeConverter::class)
    var msgType = MsgType.DEFAULT

    @Embedded
    var msgBody = MsgBody()

    @TypeConverters(MsgSendStatus.MsgSendStatusConverter::class)
    var sentStatus = MsgSendStatus.DEFAULT

}

/**
 * ping server
 */
class Ping(val msg: String = "ping")

/**
 * ping response
 */
class Pong(val msg: String = "pong")

/**
 * Received message
 */
class ReceiveMessage {
    var id: String = "-1"
    var server_id: String? = null
    var msg: String? = null
    var session: String? = null
    var collection: String? = null
    var fields: FieldsMessage? = null
    var subs: ArrayList<String>? = null
    var result: Any? = null
    var methods: ArrayList<String>? = null
    var error: ErrorResult? = null
}

class FieldsMessage {
    var eventName: String? = null

    //    var args: ArrayList<FieldsMessageBody>? = null
    var args: ArrayList<MessageHistoryResultBody>? = null
    var username: String? = null
    var emails: ArrayList<FieldsMessageBody>? = null
}

class FieldsMessageBody {
    var title: String? = null//对方用户名
    var text: String = empty
    var address: String? = null
    var verified: Boolean = false
    var payload: Payload? = null
}

class Payload {
    var _id: String? = null
    var rid: String? = null
    var type: String? = null
    var message: PayloadMessage? = null
    var sender: PayloadSender? = null
}

class PayloadSender {
    var _id: String? = null
    var username: String? = null
    var name: String? = null
}

class PayloadMessage {
    var msg: String? = null
}

class ErrorResult {
    var isClientSafe: Boolean = true
    var error: String? = null
    var reason: String? = null
    var message: String? = null
    var errorType: String? = null
    var details: ErrorResultDetail? = null
}

class ErrorResultDetail {
    var timeToReset: Long = 0
}

/**
 * MessageHistory load result
 */
class MessageHistoryResult {
    var messages: ArrayList<MessageHistoryResultBody> = arrayListOf()
    val unreadNotLoaded: Long = 0
}

class MessageHistoryResultBody {
    var _id: String? = null
    var rid: String? = null
    var msg: String = empty
    var ts: MessageDate = MessageDate()
    var u: MessageHistoryResultSender? = null
    var _updatedAt: MessageDate = MessageDate()
    var mentions: Array<Any?>? = null
    var channels: Array<Any?>? = null
    var file: FileType? = null
    val attachments: Array<Attachments> = arrayOf()
    val groupable: Boolean = false
}

class FileType(val _id: String?, val name: String?, val type: String?)

class Attachments(val ts: String?,
                  val title: String?,
                  val title_link: String?,
                  val title_link_download: Boolean = false,
                  val image_preview: String?,
                  val image_url: String?,
                  val image_type: String?,
                  val image_size: Long = 0,
                  val type: String?,
                  val description: String?,
                  val video_url: String?,
                  val video_type: String?,
                  val video_size: Long = 0,
                  @Embedded
                  val image_dimensions: ImageDimension?) {

    class AttachmentsConverter {
        @TypeConverter
        fun toJson(attachments: Array<Attachments>): String = Gson().toJson(attachments)

        @TypeConverter
        fun fromJson(attachments: String): Array<Attachments> =
                Gson().fromJson<Array<Attachments>>(attachments, object : TypeToken<Array<Attachments>>() {}.type)
    }
}

class ImageDimension(val width: Int, val height: Int)

class MessageHistoryResultSender {
    var _id: String? = null
    var username: String? = null
    var name: String? = null
}

/**
 * parse receive message
 */
class ReceiveMessageDeserilizer : JsonDeserializer<ReceiveMessage> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ReceiveMessage {
        val gson = Gson()
        val receiveMessage = gson.fromJson<ReceiveMessage>(json, ReceiveMessage::class.java)
        json?.asJsonObject?.let {
            if (it.has("result")) {
                val resultElement = it.get("result")
                if (resultElement.isJsonArray) {
                    receiveMessage.result = gson.fromJson<ArrayList<MessageHistoryResultBody>>(resultElement, object : TypeToken<ArrayList<MessageHistoryResultBody>>() {}.type)
                } else if (resultElement.isJsonObject) {
                    resultElement.asJsonObject?.let { result ->
                        if (result.has("messages")) {
                            //history data
                            receiveMessage.result = gson.fromJson(resultElement, MessageHistoryResult::class.java)
                        } else if (result.has("msg")) {
                            receiveMessage.result = gson.fromJson(resultElement, MessageHistoryResultBody::class.java)
                        }
                    }
                }
            }
        }
        return receiveMessage
    }

}




