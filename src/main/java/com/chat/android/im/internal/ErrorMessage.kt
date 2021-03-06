package com.chat.android.im.internal

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class ErrorMessage(val error: String, val errorType: String?)