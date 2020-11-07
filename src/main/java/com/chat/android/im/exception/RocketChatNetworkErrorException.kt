package com.chat.android.im.exception

import com.chat.android.im.exception.RocketChatException

class RocketChatNetworkErrorException(message: String, cause: Throwable? = null, val url: String?) : RocketChatException(message, cause, url)