package com.chat.android.im.internal

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AuthenticationErrorMessage(var message: String, var status: String, var error: String? = null)