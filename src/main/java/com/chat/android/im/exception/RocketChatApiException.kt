package com.chat.android.im.exception

class RocketChatApiException(
        val errorType: String,
        message: String,
        cause: Throwable? = null,
        url: String? = null
) : RocketChatException(message, cause, url)