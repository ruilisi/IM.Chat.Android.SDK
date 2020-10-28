package com.chat.android.im.helper

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * Created by Ryan on 2020/8/24.
 */
open class JWebSocketClient(serverUri: URI?) : WebSocketClient(serverUri, Draft_6455()) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.e("JWebSocketClient", "onOpen()")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.e("JWebSocketClient", "onClose()")
    }

    override fun onMessage(message: String?) {
        Log.e("JWebSocketClient", "onMessage()")
    }

    override fun onError(ex: Exception?) {
        Log.e("JWebSocketClient", "onError()")
    }
}