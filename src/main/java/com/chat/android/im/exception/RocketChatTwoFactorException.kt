package com.chat.android.im.exception

import com.chat.android.im.exception.RocketChatException

class RocketChatTwoFactorException(message: String, url: String? = null) : RocketChatException(message, null, url)