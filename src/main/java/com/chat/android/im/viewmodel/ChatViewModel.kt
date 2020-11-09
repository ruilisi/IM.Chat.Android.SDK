package com.chat.android.im.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore
import android.util.ArrayMap
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chat.android.im.bean.*
import com.chat.android.im.config.RLS
import com.chat.android.im.config.RLS.Companion.empty
import com.chat.android.im.constant.HEART_BEAT_RATE
import com.chat.android.im.constant.SEND_INTERVAL_NO_SHOW_TIME
import com.chat.android.im.database.DBInstance
import com.chat.android.im.helper.AndroidPermissionsHelper.getCameraPermission
import com.chat.android.im.helper.AndroidPermissionsHelper.hasCameraPermission
import com.chat.android.im.helper.IChatMessage
import com.chat.android.im.helper.IChatMessageRefresh
import com.chat.android.im.helper.JWebSocketClient
import com.chat.android.im.helper.RecycleViewScrollHelper
import com.chat.android.im.message.*
import com.chat.android.im.utils.CancelStrategy
import com.chat.android.im.utils.LogUtils
import com.chat.android.im.utils.createImageFile
import com.chat.android.im.utils.parseMsgType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.io.IOException
import java.lang.Runnable
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * Created by Ryan on 2020/8/28.
 */
class ChatViewModel : ViewModel() {

    companion object {
        const val IDLE = 0
        const val CONNECTING = 1
        const val CONNECTED = 2
        const val STOPPING = 3
        const val STOPPED = 4
        const val ERROR = 5

        const val REQUEST_CODE_FOR_DRAW = 101
        const val REQUEST_CODE_FOR_PERFORM_CAMERA = 102
        const val REQUEST_CODE_FOR_PERFORM_SAF = 42

    }

    var title: String? = null
    var gson = Gson()
    var receiveGson = GsonBuilder().registerTypeAdapter(ReceiveMessage::class.java, ReceiveMessageDeserilizer()).create()
    var count: Long = 0
    private var client: JWebSocketClient? = null
    var chatMessageData = MutableLiveData<ChatMessage>()
    var chatListMessageData = MutableLiveData<List<ChatMessage>>(arrayListOf())
    var chatMissedListMessageData = MutableLiveData<List<ChatMessage>>(arrayListOf())
    var connectState = MutableLiveData<Int>(IDLE)
    var firstRefresh = true
    var neeShowWelecome = true
    var isScrollToBottom = true
    var loadFromLocalFinished = false
    var chatSendingMessage = ArrayMap<Long, ChatMessage>()//正在发送的消息
    private var iChatMessage: IChatMessage? = null
    private val receiveLock = ReentrantLock()
    private var lastMsgId: String? = empty //偶现下发两条一模一样的数据，做下兼容
    private var lastHistoryShowTimeStamp = 0L
    private var firstHistoryShowTimeStamp = 0L
    private var lastShowTimeStamp = 0L
    var takenPhotoUri: Uri? = null
    val job = Job()

    init {
        MessageManager.setChatMessageRefreshListener(object : IChatMessageRefresh {
            override fun onSendMessage(chatMessage: ChatMessage) {
                if (chatMessage.msgType == MsgType.DEFAULT) {
                    sendMsgToServer(chatMessage.msgBody.extra)
                } else if (connectState.value == CONNECTED) {
                    sendMsgToServer(chatMessage.msgBody.extra)
                }
            }

            override fun onAddChatSendingMessage(chatMessage: ChatMessage) {
                chatSendingMessage[chatMessage.msgId] = chatMessage
            }

            override fun onRefreshView(chatMessage: ChatMessage) {
                mHandler.post {
                    chatMessageData.value = chatMessage
                }
            }

            override fun onRefreshViews(chatMessage: ArrayList<ChatMessage>, isMissedMessage: Boolean) {

                mHandler.post {
                    if (isMissedMessage) {
                        chatMissedListMessageData.value = chatMessage
                    } else {
                        chatListMessageData.value = chatMessage
                    }
                }
            }

        })
    }


    fun setIChatmessage(iChatMessage: IChatMessage?) {
        this.iChatMessage = iChatMessage
    }

    val recycleViewScrollHelper = RecycleViewScrollHelper(object : RecycleViewScrollHelper.OnScrollPositionChangedListener {
        override fun onScrollToBottom() {
            isScrollToBottom = true
        }

        override fun onScrollToTop() {
            isScrollToBottom = false
        }

        override fun onScrollToUnknown(isTopViewVisible: Boolean, isBottomViewVisible: Boolean) {
        }

        override fun onScrollStateIdle() {
            isScrollToBottom = false
        }
    })

    private val mHandler = Handler(Looper.getMainLooper()) {
        syncWithLockReciveMsg(it.obj as String?)
        false
    }

    private fun startHeartBeat(interval: Long = HEART_BEAT_RATE) {
        mHandler.removeCallbacks(heartBeatRunnable)
        mHandler.postDelayed(heartBeatRunnable, interval) //开启心跳检测
    }

    private fun generateMsg(msgType: MsgGenerageType, msg: String = empty, missedHistoryTime: Long = 0,
                            historyTime: String = empty, preLoadHistoryCount: Int = RLS.getInstance().getDataConfig().preLoadHistoryCount) = run {
        this.count = this.count.inc()
        when (msgType) {
            MsgGenerageType.CONNECT -> generateConnectMsg()
            MsgGenerageType.LOGIN -> generateLoginMsgByTokenMsg(count, RLS.getInstance().getDataConfig().token)
            MsgGenerageType.SUB_NOTIFICATION -> generateSubMsg(RLS.getInstance().getDataConfig().id, MsgGenerageType.SUB_NOTIFICATION)
            MsgGenerageType.SEND -> generateSendMsg(count, msg, RLS.getInstance().getDataConfig().rid)
            MsgGenerageType.HISTORY -> generateHistoryMsg(count, preLoadHistoryCount, RLS.getInstance().getDataConfig().rid, historyTime)
            MsgGenerageType.MISSED_HISTORY -> generateLoadMissedHistoryMsg(count, RLS.getInstance().getDataConfig().rid, missedHistoryTime)
            MsgGenerageType.SUB_ROOM_CHANGED -> generateSubMsg(RLS.getInstance().getDataConfig().id, MsgGenerageType.SUB_ROOM_CHANGED)
            MsgGenerageType.SUB_ROOM_MESSAGE -> generateSubMsg(RLS.getInstance().getDataConfig().rid, MsgGenerageType.SUB_ROOM_MESSAGE)
            MsgGenerageType.PING -> generatePing()
            MsgGenerageType.PONG -> generatePong()
        }
    }

    /**
     * 初始化websocket连接
     */
    private fun initSocketClient(): JWebSocketClient {
        val uri = URI.create(RLS.getInstance().getDataConfig().base)
        val client = object : JWebSocketClient(uri) {
            override fun onMessage(message: String?) {
                LogUtils.e("JWebSocketClientService", "收到的消息：$message")
                val msg = Message.obtain()
                msg.obj = message
                mHandler.sendMessage(msg)
            }

            override fun onOpen(handshakedata: ServerHandshake?) {
                super.onOpen(handshakedata)
                LogUtils.e("JWebSocketClientService", "websocket连接成功")
                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.CONNECT))))
                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.LOGIN))))
//                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.SUB_NOTIFICATION))))
//                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.SUB_ROOM_CHANGED))))
                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.SUB_ROOM_MESSAGE))))
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                super.onClose(code, reason, remote)
                connectState.postValue(STOPPED)
            }

            override fun onError(ex: Exception?) {
                super.onError(ex)
                connectState.postValue(ERROR)
            }
        }
        return client
    }

    /**
     * 连接websocket
     */
    private fun connect() {
        connectState.value = CONNECTING
        client = initSocketClient()
        GlobalScope.launch() {
            withContext(Dispatchers.IO) {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client?.connectBlocking()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * send message to server
     */
    private fun sendMsgToServer(msg: String?) {
        if (null != client && client!!.isOpen && msg != null) {
            LogUtils.e("JWebSocketClientService", "发送的消息：$msg")
            client!!.send(msg)
        }
    }

    /**
     * 开启重连
     */
    private fun reconnectWs() {
        connectState.value = CONNECTING
        GlobalScope.launch(Dispatchers.IO) {
            try {
                LogUtils.e("JWebSocketClientService", "开启重连")
                client!!.reconnectBlocking()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

    private val heartBeatRunnable: Runnable = object : Runnable {
        override fun run() {
            checkAndConnect()
        }
    }

    private fun checkAndConnect() {
        if (client != null) {
            if (client!!.isClosed) {
                reconnectWs()
            } else {
                MessageManager.syncWithLockSendMsg(generateBaseNormalMessage(gson.toJson(generateMsg(MsgGenerageType.PING))))
            }
        } else {
            //如果client已为空，重新初始化连接
            client = null
            connect()
        }
        //每隔一定的时间，对长连接进行一次心跳检测
        startHeartBeat()
    }

    /**
     * 断开连接
     */
    fun closeConnect() {
        try {
            client?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client = null
        }
        mHandler.removeCallbacks(heartBeatRunnable)
        MessageManager.setChatMessageRefreshListener(null)
    }

    private fun initHistoryMsgShowTimeStamp(chatMessage: ChatMessage, index: Int): ChatMessage {

        if (index == 0) {
            firstHistoryShowTimeStamp = chatMessage.ts.date
            chatMessage.timeShow.date = firstHistoryShowTimeStamp

            if (!iChatMessage?.getItemList().isNullOrEmpty()) {
                lastHistoryShowTimeStamp = iChatMessage?.getItemList()!!.first().ts.date
            } else {
                lastHistoryShowTimeStamp = System.currentTimeMillis()
            }
        } else {

            if (lastHistoryShowTimeStamp - chatMessage.ts.date > SEND_INTERVAL_NO_SHOW_TIME
                    && chatMessage.ts.date - firstHistoryShowTimeStamp > SEND_INTERVAL_NO_SHOW_TIME) {
                firstHistoryShowTimeStamp = chatMessage.ts.date
                chatMessage.timeShow.date = firstHistoryShowTimeStamp
            }

        }

        return chatMessage
    }

    private fun initMsgShowTimeStamp(chatMessage: ChatMessage): ChatMessage {
        if (lastShowTimeStamp == 0L && !iChatMessage?.getItemList().isNullOrEmpty()) {
            lastShowTimeStamp = iChatMessage?.getItemList()!!.last().ts.date
        }
        if (lastShowTimeStamp == 0L) {
            lastShowTimeStamp = System.currentTimeMillis()
            chatMessage.timeShow.date = lastShowTimeStamp
        } else {
            if (chatMessage.ts.date - lastShowTimeStamp > SEND_INTERVAL_NO_SHOW_TIME) {
                lastShowTimeStamp = chatMessage.ts.date
                chatMessage.timeShow.date = lastShowTimeStamp
            }
        }
        return chatMessage
    }

    private fun onReceiverMessage(message: String?) {
        var msg = message
        if (!msg.isNullOrEmpty()) {
            val receiveMessage = receiveGson.fromJson<ReceiveMessage>(msg, ReceiveMessage::class.java)
            when (receiveMessage.msg) {
                "connected" -> {//连接成功
                    connectState.value = CONNECTED

                    if (!chatSendingMessage.isEmpty()) {
                        MessageManager.syncWithLockSendMsg(chatSendingMessage.valueAt(0))
                    }
                }
                "changed" -> {//server send to client
                    val args = receiveMessage.fields?.args
                    if (!args.isNullOrEmpty()) {

                        if (lastMsgId == args[0]._id) {
                            return
                        }

                        lastMsgId = args[0]._id

                        iChatMessage?.getItemList()?.last()?.let {
                            if (it.msgBody.extra != empty) {
                                val sendMsg = gson.fromJson(it.msgBody.extra, SendMsg::class.java)
                                if (!sendMsg.params.isNullOrEmpty()) {
                                    if (sendMsg.params[0]._id == args[0]._id) {
                                        return
                                    }
                                }
                            }
                        }

                        iChatMessage?.getItemList()?.apply {
                            if (!none { it?.ts?.date == args[0].ts.`$date` }) return
                        }
                        this.count = this.count.inc()
                        val receiveMessage = generateBaseReceiveMessage(parseMsgType(args[0]))
                        receiveMessage.msgId = this.count
                        receiveMessage.msgStatus = if (args[0].u?._id == RLS.getInstance().getDataConfig().id) MsgStatus.SEND else MsgStatus.RECEIVE
                        receiveMessage.msgBody.message = args[0].msg
                        receiveMessage.ts.date = args[0].ts.`$date`
                        receiveMessage._updatedAt.date = args[0]._updatedAt.`$date`
                        receiveMessage.msgBody.attachments = args[0].attachments
                        MessageManager.syncWithLockSendMsg(initMsgShowTimeStamp(receiveMessage))
                    }
                }
                "result" -> {//解析历史数据
                    if (receiveMessage.result is MessageHistoryResult) {
                        updateSendMsgStatus(receiveMessage, false)

                        //history msg
                        val historyList = (receiveMessage.result as MessageHistoryResult).messages

                        if (!historyList.isNullOrEmpty()) {

                            historyList?.sortBy {
                                it.ts?.`$date`
                            }

                            val msgList = arrayListOf<ChatMessage>()
                            for (historyMsg in historyList) {
                                this.count = this.count.inc()
                                val receiveMessage = generateBaseReceiveMessage(parseMsgType(historyMsg))
                                receiveMessage.msgId = this.count
                                receiveMessage.msgStatus = if (historyMsg.u?._id == RLS.getInstance().getDataConfig().id) MsgStatus.SEND else MsgStatus.RECEIVE
                                receiveMessage.msgBody.message = historyMsg.msg
                                receiveMessage.ts.date = historyMsg.ts.`$date`
                                receiveMessage.tsc.date = historyMsg.ts.`$date`//only
                                receiveMessage._updatedAt.date = historyMsg._updatedAt.`$date`
                                receiveMessage.msgBody.attachments = historyMsg.attachments
                                msgList.add(receiveMessage)
                            }

                            for (index in msgList.indices) {
                                initHistoryMsgShowTimeStamp(msgList[index], index)
                            }

                            MessageManager.syscWithLockReceiveHistoryMsg(msgList)
                        } else {

                            val itemList = iChatMessage?.getItemList()
                            if (itemList.isNullOrEmpty()) {
                                if (neeShowWelecome) {
                                    neeShowWelecome = false
                                    this.count = this.count.inc()
                                    val chatMessage = generateBaseReceiveMessage(MsgType.TEXT)
                                    chatMessage.msgId = this.count
                                    chatMessage.msgStatus = MsgStatus.RECEIVE
                                    chatMessage.msgBody.message = RLS.getInstance().getDataConfig().welcome
                                            ?: "Welcome"
                                    MessageManager.syncWithLockSendMsg(initMsgShowTimeStamp(chatMessage))
                                }
                            } else {
                                neeShowWelecome = true
                            }
                            chatListMessageData.postValue(arrayListOf())

                        }

                        if (!chatSendingMessage.isEmpty()) {
                            MessageManager.syncWithLockSendMsg(chatSendingMessage.valueAt(0))
                        }

                    } else if (receiveMessage.result is MessageHistoryResultBody) {
                        //Server responds to client message
                        updateSendMsgStatus(receiveMessage, true)

                        if (!chatSendingMessage.isEmpty()) {
                            MessageManager.syncWithLockSendMsg(chatSendingMessage.valueAt(0))
                        }

                    } else if (receiveMessage.result is ArrayList<*>) {

                        updateSendMsgStatus(receiveMessage, false)

                        //missed msg history
                        val historyList = receiveMessage.result as ArrayList<MessageHistoryResultBody>

                        if (!historyList.isNullOrEmpty()) {

                            historyList?.sortBy {
                                it.ts?.`$date`
                            }

                            val msgList = arrayListOf<ChatMessage>()

                            val itemList = iChatMessage?.getItemList() ?: arrayListOf()

                            for (historyMsg in historyList) {
                                if (!itemList.none { it?.ts?.date == historyMsg.ts.`$date` }) continue
                                this.count = this.count.inc()
                                val receiveMessage = generateBaseReceiveMessage(parseMsgType(historyMsg))
                                receiveMessage.msgId = this.count
                                receiveMessage.msgStatus = if (historyMsg.u?._id == RLS.getInstance().getDataConfig().id) MsgStatus.SEND else MsgStatus.RECEIVE
                                receiveMessage.msgBody.message = historyMsg.msg
                                receiveMessage.ts.date = historyMsg.ts.`$date`
                                receiveMessage._updatedAt.date = historyMsg._updatedAt.`$date`
                                receiveMessage.msgBody.attachments = historyMsg.attachments

                                msgList.add(initMsgShowTimeStamp(receiveMessage))
                            }

                            MessageManager.syscWithLocakReceiveMissedHistoryMsg(msgList)

                            if (!chatSendingMessage.isEmpty()) {
                                MessageManager.syncWithLockSendMsg(chatSendingMessage.valueAt(0))
                            }
                        } else {

                            val itemList = iChatMessage?.getItemList()
                            if (itemList.isNullOrEmpty()) {
                                loadHistoryMessage(System.currentTimeMillis().toString())
                            } else {
                                if (!chatSendingMessage.isEmpty()) {
                                    MessageManager.syncWithLockSendMsg(chatSendingMessage.valueAt(0))
                                } else {
                                    chatListMessageData.postValue(arrayListOf())
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun updateSendMsgStatus(receiveMessage: ReceiveMessage, update: Boolean) {
        if (chatSendingMessage.isEmpty()) {
            return
        }

        if (!update) {
            chatSendingMessage.remove(receiveMessage.id.toLong())
            return
        }

        val itemList = iChatMessage?.getItemList()
        if (itemList.isNullOrEmpty()) {
            return
        }
        val count = itemList.size
        chatSendingMessage[receiveMessage.id.toLong()]?.let {
            for (i in count - 1 downTo 0) {
                if (itemList[i].uuid == it.uuid) {
                    chatSendingMessage.remove(receiveMessage.id.toLong())
                    if (update) {
                        itemList[i].sentStatus = MsgSendStatus.SENT
                        itemList[i].ts.date = (receiveMessage.result as MessageHistoryResultBody).ts.`$date`
                        itemList[i]._updatedAt.date = (receiveMessage.result as MessageHistoryResultBody)._updatedAt.`$date`
                        DBInstance.getInstance().getChatMessageDao().update(itemList[i])
                        iChatMessage?.notifyItemChanged(i)
                    }
                    break
                }
            }
        }

    }

    /**
     * click to send msg
     */
    fun clickSendMsg(msg: String) {
        if (msg.isNotEmpty()) {
            val sendMsg = generateMsg(MsgGenerageType.SEND, msg) as SendMsg
            val sendChatMessage = generateBaseSendMessage(MsgType.TEXT)
            sendChatMessage.msgId = sendMsg.id.toLong()
            sendChatMessage.msgBody.message = msg
            sendChatMessage.msgBody.extra = gson.toJson(sendMsg)
            MessageManager.syncWithLockSendMsg(initMsgShowTimeStamp(sendChatMessage))
        }
    }

    //开始连接
    fun startConnect() {
        if (connectState.value != CONNECTING && connectState.value != CONNECTED) {
            checkAndConnect()
        }
    }

    fun loadHistoryMessage(time: String, preLoadHistoryCount: Int = RLS.getInstance().getDataConfig().preLoadHistoryCount) {
        val historyIm = generateMsg(MsgGenerageType.HISTORY, historyTime = time, preLoadHistoryCount = preLoadHistoryCount) as HistoryIm
        val chatMessage = generateBaseNormalMessage(
                gson.toJson(historyIm), MsgType.DEFAULT_C)
        chatMessage.msgId = historyIm.id.toLong()
        MessageManager.syncWithLockSendMsg(chatMessage)
    }

    private fun syncWithLockReciveMsg(message: String?) {
        receiveLock.withLock { onReceiverMessage(message) }
    }

    fun loadMissedHistoryMessage(currentTimeMillis: Long) {
        val loadMissedHistory = generateMsg(MsgGenerageType.MISSED_HISTORY, missedHistoryTime = currentTimeMillis) as LoadMissedMessagesSend
        val chatMessage = generateBaseNormalMessage(
                gson.toJson(loadMissedHistory), MsgType.DEFAULT_C)
        chatMessage.msgId = loadMissedHistory.id.toLong()
        MessageManager.syncWithLockSendMsg(chatMessage)
    }


    fun loadLocalHistoryMsg(chatMessage: ChatMessage?) {

        val localChatMessageList: List<ChatMessage> = DBInstance.getInstance().getChatMessageDao().loadMessageOrderByDesc(
                RLS.getInstance().getDataConfig().preLoadHistoryCount, chatMessage?.tsc?.date
                ?: Long.MAX_VALUE, chatMessage?.ts?.date ?: Long.MAX_VALUE)

        arrayListOf<ChatMessage>().apply {
            addAll(localChatMessageList)
            reverse()
            loadFromLocalFinished = size < RLS.getInstance().getDataConfig().preLoadHistoryCount
            iChatMessage?.initChatMsgListView(this, !loadFromLocalFinished)
            if (chatMessage == null) {
                //first load local history
                val chatMessage = DBInstance.getInstance().getChatMessageDao().loadLastMessage()
                if (chatMessage == null) {
                    loadHistoryMessage(System.currentTimeMillis().toString())
                } else {
                    loadMissedHistoryMessage(chatMessage.ts.date)
                }
            } else {
                if (loadFromLocalFinished) {
                    if (isEmpty()) {
                        loadHistoryMessage(chatMessage.ts.date.toString())
                    } else {
                        loadHistoryMessage(first().ts.date.toString(), RLS.getInstance().getDataConfig().preLoadHistoryCount - size)
                    }
                }
            }
        }
    }

    fun openCamera(context: Activity) {
        if (hasCameraPermission(context)) {
            dispatchTakePictureIntent(context)
        } else {
            getCameraPermission(context)
        }
    }

    fun dispatchTakePictureIntent(activity: Activity) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                activity?.createImageFile()
            } catch (ex: IOException) {
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                takenPhotoUri = FileProvider.getUriForFile(
                        activity, "${activity?.packageName}.fileprovider", it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takenPhotoUri)
                activity.startActivityForResult(takePictureIntent, REQUEST_CODE_FOR_PERFORM_CAMERA)
            }
        }
    }
}