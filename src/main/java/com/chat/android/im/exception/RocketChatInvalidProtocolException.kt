package com.chat.android.im.exception

import com.chat.android.im.exception.RocketChatException

class RocketChatInvalidProtocolException(
    message: String,
    cause: Throwable? = null,
    url: String? = null
) : RocketChatException(message, cause, url)